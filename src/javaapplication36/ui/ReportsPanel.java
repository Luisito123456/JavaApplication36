package javaapplication36.ui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import javaapplication36.util.PDFGenerator2;

public class ReportsPanel extends JPanel {
    private Connection connection;
    private JTextArea reportArea;
    private JComboBox<String> reportSelector;
    private JButton btnExportPDF;
    private String currentReportContent;
    private String currentReportTitle;

    public ReportsPanel(Connection connection) {
        this.connection = connection;
        initializeUI();
        // MOVER la llamada aquí después de inicializar la UI
        // generateSalesReport(); // Eliminar esta línea del constructor
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Selector de reportes
        JPanel topPanel = new JPanel(new FlowLayout());
        reportSelector = new JComboBox<>(new String[]{
            "Reporte de Ventas",
            "Reporte de Inventario", 
            "Productos Más Vendidos",
            "Movimientos del Día",
            "Estado del Sistema"
        });
        
        // CAMBIO: "Generar Reporte" → "Actualizar"
        JButton btnGenerate = new JButton("🔄 Actualizar");
        btnExportPDF = new JButton("📄 Exportar a PDF");
        btnExportPDF.setEnabled(false);
        
        reportSelector.addActionListener(e -> generateSelectedReport());
        btnGenerate.addActionListener(e -> generateSelectedReport());
        btnExportPDF.addActionListener(e -> exportToPDF());
        
        topPanel.add(new JLabel("Seleccionar Reporte:"));
        topPanel.add(reportSelector);
        topPanel.add(btnGenerate);
        topPanel.add(btnExportPDF);
        
        add(topPanel, BorderLayout.NORTH);

        // Área de reportes
        reportArea = new JTextArea();
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setEditable(false);
        add(new JScrollPane(reportArea), BorderLayout.CENTER);
        
        // AGREGAR: Generar reporte por defecto DESPUÉS de inicializar todos los componentes
        SwingUtilities.invokeLater(() -> {
            generateSalesReport();
        });
    }

    private void generateSelectedReport() {
        String selected = (String) reportSelector.getSelectedItem();
        switch (selected) {
            case "Reporte de Ventas":
                generateSalesReport();
                break;
            case "Reporte de Inventario":
                generateInventoryReport();
                break;
            case "Productos Más Vendidos":
                generateTopProductsReport();
                break;
            case "Movimientos del Día":
                generateTodayMovementsReport();
                break;
            case "Estado del Sistema":
                generateSystemStatusReport();
                break;
        }
    }

    private void exportToPDF() {
        if (currentReportContent == null || currentReportContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No hay contenido para exportar. Genere un reporte primero.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            String fileName = PDFGenerator2.getReportFileName(currentReportTitle);
            
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new java.io.File(fileName));
            fileChooser.setDialogTitle("Guardar Reporte como PDF");
            
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Archivos PDF (*.pdf)", "pdf"));
            
            int userSelection = fileChooser.showSaveDialog(this);
            
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File selectedFile = fileChooser.getSelectedFile();
                
                if (!selectedFile.getName().toLowerCase().endsWith(".pdf")) {
                    selectedFile = new java.io.File(selectedFile.getAbsolutePath() + ".pdf");
                }
                
                if (selectedFile.exists()) {
                    int overwrite = JOptionPane.showConfirmDialog(this,
                        "El archivo ya existe. ¿Desea reemplazarlo?",
                        "Archivo existente",
                        JOptionPane.YES_NO_OPTION);
                    
                    if (overwrite != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                
                final java.io.File finalSelectedFile = selectedFile;
                final String finalCurrentReportContent = currentReportContent;
                final String finalCurrentReportTitle = currentReportTitle;
                
                JDialog progressDialog = new JDialog((Frame) null, "Exportando PDF...", true);
                progressDialog.setSize(300, 100);
                progressDialog.setLocationRelativeTo(this);
                progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                
                JPanel progressPanel = new JPanel(new BorderLayout());
                progressPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                progressPanel.add(new JLabel("Generando PDF, por favor espere...", JLabel.CENTER), BorderLayout.CENTER);
                progressDialog.add(progressPanel);
                
                new Thread(() -> {
                    boolean success = false;
                    try {
                        success = PDFGenerator2.generatePDF(finalCurrentReportContent, finalCurrentReportTitle, finalSelectedFile.getAbsolutePath());
                    } catch (Exception e) {
                        e.printStackTrace();
                        success = false;
                    }
                    
                    final boolean finalSuccess = success;
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        
                        if (finalSuccess) {
                            JOptionPane.showMessageDialog(ReportsPanel.this,
                                "✅ Reporte exportado exitosamente:\n" + finalSelectedFile.getName() +
                                "\n\nUbicación: " + finalSelectedFile.getAbsolutePath(),
                                "Éxito",
                                JOptionPane.INFORMATION_MESSAGE);
                            
                            int openFile = JOptionPane.showConfirmDialog(ReportsPanel.this,
                                "¿Desea abrir el archivo PDF ahora?",
                                "Abrir PDF",
                                JOptionPane.YES_NO_OPTION);
                            
                            if (openFile == JOptionPane.YES_OPTION) {
                                try {
                                    java.awt.Desktop.getDesktop().open(finalSelectedFile);
                                } catch (java.io.IOException ex) {
                                    JOptionPane.showMessageDialog(ReportsPanel.this,
                                        "No se pudo abrir el archivo: " + ex.getMessage(),
                                        "Error",
                                        JOptionPane.WARNING_MESSAGE);
                                }
                            }
                        } else {
                            JOptionPane.showMessageDialog(ReportsPanel.this,
                                "❌ Error al exportar el reporte.\n" +
                                "Verifique que:\n" +
                                "• Tenga permisos de escritura\n" +
                                "• La ruta sea válida\n" +
                                "• El archivo no esté en uso",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }).start();
                
                progressDialog.setVisible(true);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error inesperado: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void generateSalesReport() {
        try {
            String sql = "SELECT COUNT(*) as totalVentas, SUM(TotalAmount) as totalMonto, " +
                        "AVG(TotalAmount) as promedioVenta " +
                        "FROM Sales " +
                        "WHERE CAST(SaleDate AS DATE) = CAST(GETDATE() AS DATE)";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            StringBuilder report = new StringBuilder();
            currentReportTitle = "REPORTE DE VENTAS - " + new SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date());
            report.append("📊 ").append(currentReportTitle).append("\n");
            report.append("============================================\n\n");
            
            if (rs.next()) {
                int totalVentas = rs.getInt("totalVentas");
                double totalMonto = rs.getDouble("totalMonto");
                double promedio = rs.getDouble("promedioVenta");
                
                report.append("Ventas Hoy: ").append(totalVentas).append("\n");
                report.append("Total Vendido: $").append(String.format("%.2f", totalMonto)).append("\n");
                report.append("Promedio por Venta: $").append(String.format("%.2f", promedio)).append("\n");
            }
            
            report.append("\n🏆 PRODUCTOS MÁS VENDIDOS HOY:\n");
            report.append("----------------------------------------\n");

            String sqlTopProducts = "SELECT TOP 5 p.Name, SUM(si.Quantity) as totalVendido " +
                                   "FROM SaleItems si " +
                                   "INNER JOIN Products p ON si.ProductId = p.ProductId " +
                                   "INNER JOIN Sales s ON si.SaleId = s.SaleId " +
                                   "WHERE CAST(s.SaleDate AS DATE) = CAST(GETDATE() AS DATE) " +
                                   "GROUP BY p.Name " +
                                   "ORDER BY totalVendido DESC";

            try {
                Statement topStmt = connection.createStatement();
                ResultSet topRs = topStmt.executeQuery(sqlTopProducts);
                
                int rank = 1;
                while (topRs.next()) {
                    report.append(rank).append(". ").append(topRs.getString("Name"))
                          .append(" - ").append(topRs.getInt("totalVendido")).append(" unidades\n");
                    rank++;
                }
                
                if (rank == 1) {
                    report.append("No hay ventas hoy\n");
                }
                
                topRs.close();
                topStmt.close();
            } catch (SQLException ex) {
                report.append("Error cargando productos más vendidos hoy\n");
            }
            
            currentReportContent = report.toString();
            
            // VERIFICAR que reportArea no sea null antes de usarlo
            if (reportArea != null) {
                reportArea.setText(currentReportContent);
            }
            btnExportPDF.setEnabled(true);
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            String errorMsg = "Error generando reporte de ventas: " + e.getMessage();
            if (reportArea != null) {
                reportArea.setText(errorMsg);
            }
            currentReportContent = errorMsg;
            btnExportPDF.setEnabled(false);
            e.printStackTrace();
        }
    }

    private void generateInventoryReport() {
        try {
            StringBuilder report = new StringBuilder();
            currentReportTitle = "REPORTE DE INVENTARIO";
            report.append("📦 ").append(currentReportTitle).append("\n");
            report.append("============================================\n\n");
            
            Statement stmt = connection.createStatement();
            
            // Total productos
            String sqlTotal = "SELECT COUNT(*) as total FROM Products WHERE IsActive = 1";
            ResultSet rs = stmt.executeQuery(sqlTotal);
            rs.next();
            report.append("Total Productos: ").append(rs.getInt("total")).append("\n");
            
            // Valor total inventario
            String sqlValue = "SELECT SUM(SalePrice * Stock) as valorTotal FROM Products WHERE IsActive = 1";
            rs = stmt.executeQuery(sqlValue);
            rs.next();
            report.append("Valor Total Inventario: $").append(String.format("%.2f", rs.getDouble("valorTotal"))).append("\n");
            
            // Productos con stock bajo
            String sqlLowStock = "SELECT COUNT(*) as lowStock FROM Products WHERE Stock <= ReorderLevel AND IsActive = 1";
            rs = stmt.executeQuery(sqlLowStock);
            rs.next();
            report.append("Productos Stock Bajo: ").append(rs.getInt("lowStock")).append("\n");
            
            currentReportContent = report.toString();
            if (reportArea != null) {
                reportArea.setText(currentReportContent);
            }
            btnExportPDF.setEnabled(true);
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            String errorMsg = "Error generando reporte de inventario: " + e.getMessage();
            if (reportArea != null) {
                reportArea.setText(errorMsg);
            }
            currentReportContent = errorMsg;
            btnExportPDF.setEnabled(false);
            e.printStackTrace();
        }
    }

    private void generateTopProductsReport() {
        try {
            StringBuilder report = new StringBuilder();
            currentReportTitle = "PRODUCTOS MÁS VENDIDOS";
            report.append("🏆 ").append(currentReportTitle).append("\n");
            report.append("============================================\n\n");
            
            String sql = "SELECT TOP 10 p.Name, p.Stock, p.SalePrice, " +
                        "COALESCE(SUM(CASE WHEN im.MovementType = 'OUT' THEN im.Quantity ELSE 0 END), 0) as TotalVendido " +
                        "FROM Products p " +
                        "LEFT JOIN InventoryMovements im ON p.ProductId = im.ProductId " +
                        "WHERE p.IsActive = 1 " +
                        "GROUP BY p.ProductId, p.Name, p.Stock, p.SalePrice " +
                        "ORDER BY TotalVendido DESC, p.Name";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            int rank = 1;
            boolean hasSales = false;
            
            while (rs.next()) {
                int totalVendido = rs.getInt("TotalVendido");
                if (totalVendido > 0) {
                    hasSales = true;
                    String productName = rs.getString("Name");
                    int stock = rs.getInt("Stock");
                    double price = rs.getDouble("SalePrice");
                    
                    report.append(rank).append(". ").append(productName)
                          .append(" - Vendidos: ").append(totalVendido)
                          .append(" - Stock: ").append(stock)
                          .append(" - Precio: $").append(String.format("%.2f", price)).append("\n");
                    rank++;
                }
            }
            
            if (!hasSales) {
                report.append("No hay datos de ventas registrados en InventoryMovements.\n");
                report.append("⚠️  Las ventas no se están registrando en SaleItems.\n");
            }
            
            currentReportContent = report.toString();
            if (reportArea != null) {
                reportArea.setText(currentReportContent);
            }
            btnExportPDF.setEnabled(true);
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            String errorMsg = "Error generando reporte de productos: " + e.getMessage();
            if (reportArea != null) {
                reportArea.setText(errorMsg);
            }
            currentReportContent = errorMsg;
            btnExportPDF.setEnabled(false);
            e.printStackTrace();
        }
    }

  private void generateTodayMovementsReport() {
    try {
        StringBuilder report = new StringBuilder();
        currentReportTitle = "MOVIMIENTOS DEL DÍA";
        report.append("📋 ").append(currentReportTitle).append("\n");
        report.append("============================================\n\n");
        
        // Obtener la fecha actual 
        java.util.Date today = new java.util.Date();
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String todayDisplay = displayFormat.format(today);
        String todayDB = dbDateFormat.format(today);
        
        report.append("Fecha: ").append(todayDisplay).append("\n\n");
        
        System.out.println("🔍 Buscando movimientos para: " + todayDB);
        
        // CONSULTA CORREGIDA: Usar parámetros preparados correctamente
        String sqlMovements = "SELECT p.Name, im.MovementType, im.Quantity, u.Username, im.CreatedAt " +
                    "FROM InventoryMovements im " +
                    "INNER JOIN Products p ON im.ProductId = p.ProductId " +
                    "INNER JOIN Users u ON im.UserId = u.UserId " +
                    "WHERE CONVERT(DATE, im.CreatedAt) = CONVERT(DATE, GETDATE()) " +
                    "ORDER BY im.CreatedAt DESC";
        
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sqlMovements);
        
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        boolean hasMovements = false;
        int movementCount = 0;
        
        while (rs.next()) {
            hasMovements = true;
            movementCount++;
            String tipo = rs.getString("MovementType").equals("IN") ? "📥 Entrada" : "📤 Salida";
            String productName = rs.getString("Name");
            int quantity = rs.getInt("Quantity");
            String username = rs.getString("Username");
            String hora = timeFormat.format(rs.getTimestamp("CreatedAt"));
            
            report.append("• ").append(productName)
                  .append(" - ").append(tipo)
                  .append(" - ").append(quantity).append(" unidades")
                  .append(" - ").append(username)
                  .append(" - ").append(hora).append("\n");
        }
        
        rs.close();
        stmt.close();
        
        System.out.println("📊 Movimientos encontrados: " + movementCount);
        
        if (!hasMovements) {
            report.append("No hay movimientos de inventario hoy\n");
        }
        
        // También mostrar ventas del día como referencia
        report.append("\n--- VENTAS DEL DÍA ---\n");
        
        String sqlSales = "SELECT s.SaleId, s.TotalAmount, s.SaleDate, s.PaymentMethod, " +
                         "COALESCE(s.ClienteNombre, 'Cliente Mostrador') as Cliente " +
                         "FROM Sales s " +
                         "WHERE CONVERT(DATE, s.SaleDate) = CONVERT(DATE, GETDATE()) " +
                         "ORDER BY s.SaleDate DESC";
        
        Statement stmtSales = connection.createStatement();
        ResultSet rsSales = stmtSales.executeQuery(sqlSales);
        
        boolean hasSales = false;
        double totalVentas = 0;
        int ventasCount = 0;
        
        while (rsSales.next()) {
            hasSales = true;
            ventasCount++;
            double total = rsSales.getDouble("TotalAmount");
            totalVentas += total;
            String cliente = rsSales.getString("Cliente");
            String metodoPago = rsSales.getString("PaymentMethod");
            String horaVenta = timeFormat.format(rsSales.getTimestamp("SaleDate"));
            
            report.append("💰 Venta #").append(rsSales.getInt("SaleId"))
                  .append(" - ").append(cliente)
                  .append(" - $").append(String.format("%.2f", total))
                  .append(" - ").append(metodoPago)
                  .append(" - ").append(horaVenta).append("\n");
        }
        
        rsSales.close();
        stmtSales.close();
        
        if (hasSales) {
            report.append("\n📈 RESUMEN: ").append(ventasCount)
                  .append(" ventas - Total: $").append(String.format("%.2f", totalVentas));
        } else {
            report.append("No hay ventas hoy");
        }
        
        currentReportContent = report.toString();
        if (reportArea != null) {
            reportArea.setText(currentReportContent);
        }
        btnExportPDF.setEnabled(true);
        
        System.out.println("✅ Reporte generado exitosamente");
        
    } catch (SQLException e) {
        String errorMsg = "Error generando reporte de movimientos: " + e.getMessage();
        System.err.println("❌ Error: " + errorMsg);
        if (reportArea != null) {
            reportArea.setText(errorMsg);
        }
        currentReportContent = errorMsg;
        btnExportPDF.setEnabled(false);
        e.printStackTrace();
    }
}

    private void generateSystemStatusReport() {
        try {
            StringBuilder report = new StringBuilder();
            currentReportTitle = "ESTADO DEL SISTEMA";
            report.append("🖥️  ").append(currentReportTitle).append("\n");
            report.append("============================================\n\n");
            
            Statement stmt = connection.createStatement();
            
            // Conteo de registros por tabla
            String[] tablas = {"Products", "Sales", "Customers", "InventoryMovements", "Users"};
            
            for (String tabla : tablas) {
                String sql = "SELECT COUNT(*) as total FROM " + tabla;
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    report.append(tabla).append(": ").append(rs.getInt("total")).append(" registros\n");
                }
                rs.close();
            }
            
            report.append("\n✅ Sistema operativo correctamente");
            
            currentReportContent = report.toString();
            if (reportArea != null) {
                reportArea.setText(currentReportContent);
            }
            btnExportPDF.setEnabled(true);
            
            stmt.close();
            
        } catch (SQLException e) {
            String errorMsg = "Error generando estado del sistema: " + e.getMessage();
            if (reportArea != null) {
                reportArea.setText(errorMsg);
            }
            currentReportContent = errorMsg;
            btnExportPDF.setEnabled(false);
            e.printStackTrace();
        }
    }
}