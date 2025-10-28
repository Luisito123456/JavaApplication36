package javaapplication36.ui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class ReportsPanel extends JPanel {
    private Connection connection;
    private JTextArea reportArea;
    private JComboBox<String> reportSelector;

    public ReportsPanel(Connection connection) {
        this.connection = connection;
        initializeUI();
        generateSalesReport(); // Reporte por defecto
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
        
        JButton btnGenerate = new JButton("Generar Reporte");
        
        reportSelector.addActionListener(e -> generateSelectedReport());
        btnGenerate.addActionListener(e -> generateSelectedReport());
        
        topPanel.add(new JLabel("Seleccionar Reporte:"));
        topPanel.add(reportSelector);
        topPanel.add(btnGenerate);
        
        add(topPanel, BorderLayout.NORTH);

        // Área de reportes
        reportArea = new JTextArea();
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setEditable(false);
        add(new JScrollPane(reportArea), BorderLayout.CENTER);
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

   private void generateSalesReport() {
    try {
        String sql = "SELECT COUNT(*) as totalVentas, SUM(TotalAmount) as totalMonto, " +
                    "AVG(TotalAmount) as promedioVenta " +
                    "FROM Sales " +
                    "WHERE CAST(SaleDate AS DATE) = CAST(GETDATE() AS DATE)";
        
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        StringBuilder report = new StringBuilder();
        report.append("📊 REPORTE DE VENTAS - ").append(new SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date())).append("\n");
        report.append("============================================\n\n");
        
        if (rs.next()) {
            int totalVentas = rs.getInt("totalVentas");
            double totalMonto = rs.getDouble("totalMonto");
            double promedio = rs.getDouble("promedioVenta");
            
            report.append("Ventas Hoy: ").append(totalVentas).append("\n");
            report.append("Total Vendido: $").append(String.format("%.2f", totalMonto)).append("\n");
            report.append("Promedio por Venta: $").append(String.format("%.2f", promedio)).append("\n");
        }
        
        // Busca esta sección y descoméntala/modifícala:
report.append("\n🏆 PRODUCTOS MÁS VENDIDOS HOY:\n");
report.append("----------------------------------------\n");

// REEMPLAZA ESTO:
report.append("(Funcionalidad de productos más vendidos en desarrollo)\n");

// POR ESTO:
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
        reportArea.setText(report.toString());
        rs.close();
        stmt.close();
        
    } catch (SQLException e) {
        reportArea.setText("Error generando reporte de ventas: " + e.getMessage());
        e.printStackTrace();
    }
}

    private void generateInventoryReport() {
    try {
        StringBuilder report = new StringBuilder();
        report.append("📦 REPORTE DE INVENTARIO\n");
        report.append("============================================\n\n");
        
        Statement stmt = connection.createStatement();
        
        // Total productos
        String sqlTotal = "SELECT COUNT(*) as total FROM Products WHERE IsActive = 1";
        ResultSet rs = stmt.executeQuery(sqlTotal);
        rs.next();
        report.append("Total Productos: ").append(rs.getInt("total")).append("\n");
        
        // Valor total inventario - CORREGIDO
        String sqlValue = "SELECT SUM(SalePrice * Stock) as valorTotal FROM Products WHERE IsActive = 1";
        rs = stmt.executeQuery(sqlValue);
        rs.next();
        report.append("Valor Total Inventario: $").append(String.format("%.2f", rs.getDouble("valorTotal"))).append("\n");
        
        // Productos con stock bajo - CORREGIDO
        String sqlLowStock = "SELECT COUNT(*) as lowStock FROM Products WHERE Stock <= ReorderLevel AND IsActive = 1";
        rs = stmt.executeQuery(sqlLowStock);
        rs.next();
        report.append("Productos Stock Bajo: ").append(rs.getInt("lowStock")).append("\n");
        
        reportArea.setText(report.toString());
        rs.close();
        stmt.close();
        
    } catch (SQLException e) {
        reportArea.setText("Error generando reporte de inventario: " + e.getMessage());
        e.printStackTrace();
    }
}

  private void generateTopProductsReport() {
    try {
        StringBuilder report = new StringBuilder();
        report.append("🏆 PRODUCTOS MÁS VENDIDOS (Basado en movimientos OUT)\n");
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
        
        reportArea.setText(report.toString());
        rs.close();
        stmt.close();
        
    } catch (SQLException e) {
        reportArea.setText("Error generando reporte de productos: " + e.getMessage());
        e.printStackTrace();
    }
}
private void generateTodayMovementsReport() {
    try {
        StringBuilder report = new StringBuilder();
        report.append("📋 MOVIMIENTOS DE HOY\n");
        report.append("============================================\n\n");
        
        String sql = "SELECT im.MovementId, p.Name, im.MovementType, " +
                    "im.Quantity, u.Username, im.CreatedAt " +
                    "FROM InventoryMovements im " +
                    "INNER JOIN Products p ON im.ProductId = p.ProductId " +
                    "INNER JOIN Users u ON im.UserId = u.UserId " +
                    "WHERE CAST(im.CreatedAt AS DATE) = CAST(GETDATE() AS DATE) " +
                    "ORDER BY im.CreatedAt DESC";
        
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        
        while (rs.next()) {
            String tipo = rs.getString("MovementType").equals("IN") ? "📥 Entrada" : "📤 Salida";
            report.append("• ").append(rs.getString("Name"))
                  .append(" - ").append(tipo)
                  .append(" - ").append(rs.getInt("Quantity")).append(" unidades")
                  .append(" - ").append(rs.getString("Username"))
                  .append(" - ").append(dateFormat.format(rs.getTimestamp("CreatedAt"))).append("\n");
        }
        
        if (report.toString().equals("📋 MOVIMIENTOS DE HOY\n============================================\n\n")) {
            report.append("No hay movimientos hoy");
        }
        
        reportArea.setText(report.toString());
        rs.close();
        stmt.close();
        
    } catch (SQLException e) {
        reportArea.setText("Error generando reporte de movimientos: " + e.getMessage());
    }
}

    
    private void generateSystemStatusReport() {
        try {
            StringBuilder report = new StringBuilder();
            report.append("🖥️  ESTADO DEL SISTEMA\n");
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
            reportArea.setText(report.toString());
            stmt.close();
            
        } catch (SQLException e) {
            reportArea.setText("Error generando estado del sistema: " + e.getMessage());
        }
    }
}