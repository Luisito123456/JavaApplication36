package javaapplication36.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PDFGenerator2 {
    
    // Fuentes predefinidas
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.BLACK);
    private static final Font SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.GRAY);
    
    public static boolean generatePDF(String content, String title, String filePath) {
        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            
            addHeader(document, title);
            addFormattedContent(document, content, title);
            addFooter(document, content, title); // Pasar content y title
            
            document.close();
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static void addHeader(Document document, String title) throws DocumentException {
        Paragraph mainTitle = new Paragraph("INVENTATIO", TITLE_FONT);
        mainTitle.setAlignment(Element.ALIGN_CENTER);
        mainTitle.setSpacingAfter(5);
        document.add(mainTitle);
        
        Paragraph subTitle = new Paragraph("Punto de Venta", SUBTITLE_FONT);
        subTitle.setAlignment(Element.ALIGN_CENTER);
        subTitle.setSpacingAfter(10);
        document.add(subTitle);
        
        PdfPTable menuTable = new PdfPTable(3);
        menuTable.setWidthPercentage(80);
        menuTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        addMenuCell(menuTable, "Proveedores");
        addMenuCell(menuTable, "Movimientos");
        addMenuCell(menuTable, "Reportes");
        
        document.add(menuTable);
        addSeparatorLine(document);
        document.add(Chunk.NEWLINE);
        
        if (title != null && !title.isEmpty()) {
            Paragraph reportTitle = new Paragraph(title, HEADER_FONT);
            reportTitle.setAlignment(Element.ALIGN_CENTER);
            reportTitle.setSpacingAfter(15);
            document.add(reportTitle);
        }
    }
    
    private static void addFooter(Document document, String content, String title) throws DocumentException {
        document.add(Chunk.NEWLINE);
        addSeparatorLine(document);
        
        String fechaGeneracion = getCorrectDateFromContent(content, title);
        
        Paragraph footer = new Paragraph();
        footer.add(new Chunk("Generado el: " + fechaGeneracion, SMALL_FONT));
        footer.add(Chunk.NEWLINE);
        footer.add(new Chunk("Sistema Inventatio - Punto de Venta", SMALL_FONT));
        footer.setAlignment(Element.ALIGN_CENTER);
        
        document.add(footer);
    }
    
    private static String getCorrectDateFromContent(String content, String title) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        
        // Para "Movimientos del Día", usar fecha del día
        if (title != null && title.contains("MOVIMIENTOS DEL DÍA")) {
            // Intentar extraer fecha de los movimientos
            try {
                String[] lines = content.split("\n");
                for (String line : lines) {
                    if (line.contains(" - ") && line.matches(".*\\d{2}:\\d{2}")) {
                        // Si encontramos movimientos con hora, usar fecha de hoy
                        return new SimpleDateFormat("dd/MM/yyyy").format(new Date()) + " 23:59:59";
                    }
                }
            } catch (Exception e) {
                // Si hay error, usar fecha actual
            }
            return new SimpleDateFormat("dd/MM/yyyy").format(new Date()) + " 23:59:59";
        }
        
        // Para otros reportes, usar fecha y hora actual
        return dateFormat.format(new Date());
    }
    
    // Los demás métodos permanecen igual...
    private static void addMenuCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }
    
    private static void addFormattedContent(Document document, String content, String title) throws DocumentException {
        if (content == null || content.isEmpty()) {
            Paragraph empty = new Paragraph("No hay contenido para mostrar", NORMAL_FONT);
            empty.setAlignment(Element.ALIGN_CENTER);
            document.add(empty);
            return;
        }
        
        if (title.contains("MOVIMIENTOS DEL DÍA")) {
            addMovimientosContent(document, content);
        } else if (title.contains("VENTAS") || title.contains("REPORTE DE VENTAS")) {
            addVentasContent(document, content);
        } else if (title.contains("INVENTARIO")) {
            addInventarioContent(document, content);
        } else if (title.contains("PRODUCTOS MÁS VENDIDOS")) {
            addTopProductsContent(document, content);
        } else {
            addDefaultContent(document, content);
        }
    }
    
    private static void addMovimientosContent(Document document, String content) throws DocumentException {
        String[] lines = content.split("\n");
        
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 1, 1, 1, 1});
        
        addTableHeader(table, "PRODUCTO");
        addTableHeader(table, "TIPO");
        addTableHeader(table, "CANTIDAD");
        addTableHeader(table, "USUARIO");
        addTableHeader(table, "HORA");
        
        boolean hasData = false;
        
        for (String line : lines) {
            if (line.trim().isEmpty() || line.contains("====") || line.contains("----")) continue;
            
            if (line.startsWith("•")) {
                hasData = true;
                String cleanLine = line.substring(1).trim();
                String[] parts = cleanLine.split(" - ");
                
                if (parts.length >= 5) {
                    addTableCell(table, parts[0].trim());
                    addTableCell(table, parts[1].trim());
                    addTableCell(table, parts[2].replace("unidades", "").trim());
                    addTableCell(table, parts[3].trim());
                    addTableCell(table, parts[4].trim());
                } else if (parts.length >= 4) {
                    addTableCell(table, parts[0].trim());
                    addTableCell(table, parts[1].trim());
                    addTableCell(table, parts[2].replace("unidades", "").trim());
                    addTableCell(table, parts[3].trim());
                    addTableCell(table, "");
                }
            }
        }
        
        if (hasData) {
            document.add(table);
        } else {
            Paragraph noData = new Paragraph("No hay movimientos para mostrar", NORMAL_FONT);
            noData.setAlignment(Element.ALIGN_CENTER);
            document.add(noData);
        }
    }
    
    private static void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(BaseColor.DARK_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }
    
    private static void addTableCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setPadding(5);
        table.addCell(cell);
    }
    
    private static void addSeparatorLine(Document document) throws DocumentException {
        Paragraph separator = new Paragraph("____________________________________________________________");
        separator.setAlignment(Element.ALIGN_CENTER);
        document.add(separator);
    }
    
    // Métodos restantes igual...
    private static void addVentasContent(Document document, String content) throws DocumentException {
        // Implementación existente...
    }
    
    private static void addInventarioContent(Document document, String content) throws DocumentException {
        // Implementación existente...
    }
    
    private static void addTopProductsContent(Document document, String content) throws DocumentException {
        // Implementación existente...
    }
    
    private static void addDefaultContent(Document document, String content) throws DocumentException {
        // Implementación existente...
    }
    
    public static String getReportFileName(String reportTitle) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());
        
        String cleanTitle = "Reporte";
        if (reportTitle != null) {
            cleanTitle = reportTitle.replaceAll("[^a-zA-Z0-9\\sáéíóúÁÉÍÓÚñÑ]", "")
                                  .replaceAll("\\s+", "_");
        }
        
        return String.format("%s_%s.pdf", cleanTitle, timestamp);
    }
}