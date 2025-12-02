package javaapplication36.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PDFGenerator {
    
    public static boolean generateInvoicePDF(String content, String title, String fileName, 
                                           String clienteNombre, String clienteDNI, String clienteRUC,
                                           String vendedor, String metodoPago) {
        Document document = null;
        FileOutputStream fos = null;
        
        try {
            // Crear documento
            document = new Document(PageSize.A4, 40, 40, 60, 40);
            fos = new FileOutputStream(fileName);
            PdfWriter writer = PdfWriter.getInstance(document, fos);
            
            document.open();
            
            // Configurar colores
            BaseColor primaryColor = new BaseColor(44, 62, 80);    // Azul oscuro
            BaseColor accentColor = new BaseColor(231, 76, 60);    // Rojo
            BaseColor darkGray = new BaseColor(64, 64, 64);
            BaseColor lightGray = new BaseColor(240, 240, 240);
            
            // ==================== ENCABEZADO ====================
            addInvoiceHeader(document, primaryColor, accentColor);
            
            // ==================== INFORMACIÓN DEL CLIENTE ====================
            addClientInfo(document, clienteNombre, clienteDNI, clienteRUC, darkGray);
            
            // ==================== DETALLES DE LA FACTURA ====================
            addInvoiceDetails(document, title, vendedor, metodoPago, darkGray);
            
            // ==================== TABLA DE PRODUCTOS ====================
            addProductsTable(document, content, primaryColor, lightGray);
            
            // ==================== TOTALES ====================
            addInvoiceTotals(document, content, accentColor);
            
            // ==================== PIE DE PÁGINA ====================
            addInvoiceFooter(document, darkGray);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ ERROR generando factura PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (document != null && document.isOpen()) {
                document.close();
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    System.err.println("Error cerrando FileOutputStream: " + e.getMessage());
                }
            }
        }
    }
    
    private static void addInvoiceHeader(Document document, BaseColor primaryColor, BaseColor accentColor) 
            throws DocumentException {
        
        // Fuentes
        Font companyFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, primaryColor);
        Font taglineFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, primaryColor);
        Font contactFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, primaryColor);
        
        // Tabla del encabezado
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setSpacingBefore(10);
        headerTable.setSpacingAfter(20);
        
        // Columna izquierda - Información de la empresa
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        
        Paragraph company = new Paragraph("LICORERÍA BARTENDER", companyFont);
        company.setSpacingAfter(5);
        
        Paragraph tagline = new Paragraph("SISTEMA INTEGRADO POS", taglineFont);
        tagline.setSpacingAfter(10);
        
        Paragraph address = new Paragraph("Av. Constelación Austral 131 - Lima, Perú", contactFont);
        address.setSpacingAfter(2);
        
        Paragraph phone = new Paragraph("Teléfono: (01) 654 - 5485", contactFont);
        phone.setSpacingAfter(2);
        
        Paragraph email = new Paragraph("Email: info@licoreriabartender.com", contactFont);
        email.setSpacingAfter(2);
        
        Paragraph ruc = new Paragraph("RUC: 20123456789", contactFont);
        
        leftCell.addElement(company);
        leftCell.addElement(tagline);
        leftCell.addElement(address);
        leftCell.addElement(phone);
        leftCell.addElement(email);
        leftCell.addElement(ruc);
        
        // Columna derecha - Logo/Espacio
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        Paragraph logoSpace = new Paragraph("", 
            new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.LIGHT_GRAY));
        logoSpace.setSpacingAfter(5);
        
        rightCell.addElement(logoSpace);
        
        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);
        
        document.add(headerTable);
        
        // Línea separadora
        addSeparatorLine(document, primaryColor, 2);
    }
    
    private static void addClientInfo(Document document, String nombre, String dni, String ruc, BaseColor darkGray) 
            throws DocumentException {
        
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, darkGray);
        Font infoFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
        
        Paragraph clientSection = new Paragraph("INFORMACIÓN DEL CLIENTE", sectionFont);
        clientSection.setSpacingAfter(10);
        document.add(clientSection);
        
        // Tabla de información del cliente
        PdfPTable clientTable = new PdfPTable(2);
        clientTable.setWidthPercentage(100);
        clientTable.setSpacingAfter(15);
        
        addClientRow(clientTable, "DATOS DEL CLIENTE:", nombre, infoFont);
        if (dni != null && !dni.isEmpty()) {
            addClientRow(clientTable, "DNI:", dni, infoFont);
        }
        if (ruc != null && !ruc.isEmpty()) {
            addClientRow(clientTable, "RUC:", ruc, infoFont);
        }
        
        document.add(clientTable);
        
        addSeparatorLine(document, BaseColor.LIGHT_GRAY, 1);
    }
    
    private static void addClientRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Paragraph(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setBackgroundColor(new BaseColor(250, 250, 250));
        labelCell.setPadding(5);
        
        String displayValue = (value != null && !value.isEmpty()) ? value : "No especificado";
        PdfPCell valueCell = new PdfPCell(new Paragraph(displayValue, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
    
    private static void addInvoiceDetails(Document document, String title, String vendedor, String metodoPago, BaseColor darkGray) 
            throws DocumentException {
        
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, darkGray);
        Font detailFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
        
        Paragraph invoiceTitle = new Paragraph(title, titleFont);
        invoiceTitle.setAlignment(Element.ALIGN_CENTER);
        invoiceTitle.setSpacingAfter(15);
        document.add(invoiceTitle);
        
        // Tabla de detalles
        PdfPTable detailsTable = new PdfPTable(4);
        detailsTable.setWidthPercentage(100);
        detailsTable.setSpacingAfter(20);
        
        String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
        
        addDetailRow(detailsTable, "FECHA EMISIÓN:", fecha, detailFont);
        addDetailRow(detailsTable, "VENDEDOR:", vendedor, detailFont);
        addDetailRow(detailsTable, "MÉTODO PAGO:", metodoPago, detailFont);
        addDetailRow(detailsTable, "TIPO COMPROBANTE:", "FACTURA ELECTRÓNICA", detailFont);
        
        document.add(detailsTable);
    }
    
    private static void addDetailRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Paragraph(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setBackgroundColor(new BaseColor(245, 245, 245));
        labelCell.setPadding(5);
        
        PdfPCell valueCell = new PdfPCell(new Paragraph(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
    
    private static void addProductsTable(Document document, String content, BaseColor primaryColor, BaseColor lightGray) 
            throws DocumentException {
        
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.BLACK);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.BLACK);
        
        Paragraph productsTitle = new Paragraph("DETALLE DE PRODUCTOS", 
            new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, primaryColor));
        productsTitle.setSpacingAfter(10);
        document.add(productsTitle);
        
        // Crear tabla de productos
        PdfPTable productsTable = new PdfPTable(5);
        productsTable.setWidthPercentage(100);
        productsTable.setSpacingAfter(20);
        productsTable.setWidths(new float[]{3, 1, 1.5f, 1, 1.5f});
        
        // Encabezados de la tabla
        String[] headers = {"DESCRIPCIÓN", "CANT.", "P. UNITARIO", "DSCTO.", "SUBTOTAL"};
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Paragraph(header, headerFont));
            headerCell.setBackgroundColor(primaryColor);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setPadding(8);
            productsTable.addCell(headerCell);
        }
        
        // Procesar contenido para extraer productos
        String[] lines = content.split("\n");
        double subtotalTotal = 0;
        boolean hasProducts = false;
        
        for (String line : lines) {
            if (line.contains("|")) {
                // Formato: Producto|cantidad|precio|subtotal
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    hasProducts = true;
                    String descripcion = parts[0].trim();
                    String cantidad = parts[1].trim();
                    double precio = Double.parseDouble(parts[2]);
                    double subtotal = Double.parseDouble(parts[3]);
                    subtotalTotal += subtotal;
                    
                    addProductRow(productsTable, descripcion, cantidad, 
                                String.format("$%.2f", precio), 
                                "$0.00", 
                                String.format("$%.2f", subtotal), 
                                normalFont);
                }
            }
        }
        
        if (hasProducts) {
            document.add(productsTable);
        } else {
            Paragraph noProducts = new Paragraph("No hay productos en esta venta", normalFont);
            noProducts.setAlignment(Element.ALIGN_CENTER);
            document.add(noProducts);
        }
    }
    
    private static void addProductRow(PdfPTable table, String descripcion, String cantidad, 
                                    String precioUnitario, String descuento, String subtotal, Font font) {
        
        PdfPCell descCell = new PdfPCell(new Paragraph(descripcion, font));
        descCell.setPadding(6);
        
        PdfPCell cantCell = new PdfPCell(new Paragraph(cantidad, font));
        cantCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cantCell.setPadding(6);
        
        PdfPCell priceCell = new PdfPCell(new Paragraph(precioUnitario, font));
        priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        priceCell.setPadding(6);
        
        PdfPCell discountCell = new PdfPCell(new Paragraph(descuento, font));
        discountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        discountCell.setPadding(6);
        
        PdfPCell subtotalCell = new PdfPCell(new Paragraph(subtotal, font));
        subtotalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        subtotalCell.setPadding(6);
        
        table.addCell(descCell);
        table.addCell(cantCell);
        table.addCell(priceCell);
        table.addCell(discountCell);
        table.addCell(subtotalCell);
    }
    
    private static void addInvoiceTotals(Document document, String content, BaseColor accentColor) 
            throws DocumentException {
        
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);
        Font valueFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);
        Font totalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, accentColor);
        
        // Calcular total del contenido
        double total = 0;
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.contains("|")) {
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    total += Double.parseDouble(parts[3]);
                }
            }
        }
        
        // Calcular IGV (18%)
        double subtotal = total / 1.18;
        double igv = total - subtotal;
        
        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(50);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.setSpacingBefore(10);
        
        addTotalRow(totalsTable, "SUBTOTAL:", String.format("$%.2f", subtotal), labelFont, valueFont);
        addTotalRow(totalsTable, "IGV (18%):", String.format("$%.2f", igv), labelFont, valueFont);
        
        // Línea separadora para el total
        PdfPCell emptyCell1 = new PdfPCell(new Paragraph(""));
        emptyCell1.setBorder(Rectangle.NO_BORDER);
        PdfPCell emptyCell2 = new PdfPCell(new Paragraph(""));
        emptyCell2.setBorder(Rectangle.NO_BORDER);
        totalsTable.addCell(emptyCell1);
        totalsTable.addCell(emptyCell2);
        
        addTotalRow(totalsTable, "TOTAL:", String.format("$%.2f", total), totalFont, totalFont);
        
        document.add(totalsTable);
    }
    
    private static void addTotalRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Paragraph(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        
        PdfPCell valueCell = new PdfPCell(new Paragraph(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(5);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
    
    private static void addInvoiceFooter(Document document, BaseColor darkGray) 
            throws DocumentException {
        
        Paragraph footer = new Paragraph();
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        
        Font thankYouFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLDITALIC, darkGray);
        Font infoFont = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY);
        
        Chunk thanks = new Chunk("¡Gracias por su preferencia!\n", thankYouFont);
        footer.add(thanks);
        
        footer.add(Chunk.NEWLINE);
        Chunk legal = new Chunk("Este documento es una representación impresa de un comprobante electrónico\n", infoFont);
        footer.add(legal);
        
        Chunk conditions = new Chunk("Válido como comprobante de pago - No válido como crédito fiscal", infoFont);
        footer.add(conditions);
        
        footer.add(Chunk.NEWLINE);
        Chunk pageInfo = new Chunk("Página 1", 
            new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.GRAY));
        footer.add(pageInfo);
        
        document.add(footer);
    }
    
    private static void addSeparatorLine(Document document, BaseColor color, float thickness) 
            throws DocumentException {
        
        Paragraph line = new Paragraph("________________________________________________________________");
        line.getFont().setColor(color);
        line.setSpacingAfter(10);
        line.setSpacingBefore(5);
        document.add(line);
    }
    
    // Método original para reportes (mantener compatibilidad)
    public static boolean generatePDF(String content, String title, String fileName) {
        return generateInvoicePDF(content, title, fileName, 
            "Cliente Mostrador", "", "", "Sistema POS", "Efectivo");
    }
    
    public static String getReportFileName(String reportType) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String cleanName = reportType.replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("\\s+", "_");
        return "comprobante_" + cleanName + "_" + timestamp + ".pdf";
    }
}