package javaapplication36.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ProductPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private Connection connection;
    private JLabel lblStats;

    public ProductPanel(Connection connection) {
        this.connection = connection;
        initializeUI();
        loadProducts();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de estadísticas
        lblStats = new JLabel("Cargando productos...");
        lblStats.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(lblStats, BorderLayout.NORTH);

        // Tabla de productos - COLUMNAS CORREGIDAS
        model = new DefaultTableModel(new Object[]{
            "ID", "SKU", "Nombre", "Categoría", "Proveedor", 
            "Precio Compra", "Precio Venta", "Stock", "Stock Mínimo", "Estado"
        }, 0);
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnRefresh = new JButton("🔄 Actualizar");
        JButton btnAddProduct = new JButton("➕ Agregar Producto");
        JButton btnEditProduct = new JButton("✏️ Editar");
        JButton btnLowStock = new JButton("⚠️ Stock Bajo");
        
        btnRefresh.addActionListener(e -> loadProducts());
        btnAddProduct.addActionListener(e -> showAddProductDialog());
        btnLowStock.addActionListener(e -> showLowStockProducts());
        
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnAddProduct);
        buttonPanel.add(btnEditProduct);
        buttonPanel.add(btnLowStock);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadProducts() {
        try {
            
            String sql = "SELECT p.ProductId, p.SKU, p.Name, " +
                        "COALESCE(c.Name, 'Sin Categoría') as Categoria, " +
                        "COALESCE(s.Name, 'Sin Proveedor') as Proveedor, " +
                        "p.CostPrice, p.SalePrice, p.Stock, p.ReorderLevel, " +
                        "CASE WHEN p.IsActive = 1 THEN 'Activo' ELSE 'Inactivo' END as Estado " +
                        "FROM Products p " +
                        "LEFT JOIN Categories c ON p.CategoryId = c.CategoryId " +
                        "LEFT JOIN Suppliers s ON p.SupplierId = s.SupplierId " +
                        "ORDER BY p.Name";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            model.setRowCount(0);
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("ProductId"),
                    rs.getString("SKU"),
                    rs.getString("Name"),
                    rs.getString("Categoria"),
                    rs.getString("Proveedor"),
                    String.format("$%.2f", rs.getDouble("CostPrice")),
                    String.format("$%.2f", rs.getDouble("SalePrice")),
                    rs.getInt("Stock"),
                    rs.getInt("ReorderLevel"),
                    rs.getString("Estado")
                });
            }
            
            rs.close();
            stmt.close();
            
            updateStatistics();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error cargando productos: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Para ver el error completo en consola
        }
    }

    private void updateStatistics() {
        try {
            // Total productos
            String sqlTotal = "SELECT COUNT(*) as total FROM Products WHERE IsActive = 1";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sqlTotal);
            rs.next();
            int totalProducts = rs.getInt("total");
            
            // Productos con stock bajo 
            String sqlLowStock = "SELECT COUNT(*) as lowStock FROM Products WHERE Stock <= ReorderLevel AND IsActive = 1";
            rs = stmt.executeQuery(sqlLowStock);
            rs.next();
            int lowStock = rs.getInt("lowStock");
            
            // Valor total del inventario 
            String sqlTotalValue = "SELECT SUM(SalePrice * Stock) as totalValue FROM Products WHERE IsActive = 1";
            rs = stmt.executeQuery(sqlTotalValue);
            rs.next();
            double totalValue = rs.getDouble("totalValue");
            
            lblStats.setText(String.format(
                "📦 Productos: %d total | ⚠️ %d con stock bajo | 💰 Valor inventario: $%.2f", 
                totalProducts, lowStock, totalValue
            ));
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            lblStats.setText("Error cargando estadísticas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAddProductDialog() {
        JOptionPane.showMessageDialog(this, 
            "Funcionalidad para agregar productos - Próximamente",
            "En desarrollo", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void showLowStockProducts() {
        try {
            String sql = "SELECT Name, Stock, ReorderLevel " +
                        "FROM Products WHERE Stock <= ReorderLevel AND IsActive = 1 " +
                        "ORDER BY Stock ASC";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            StringBuilder sb = new StringBuilder();
            sb.append("⚠️ Productos con stock bajo:\n\n");
            
            while (rs.next()) {
                sb.append(String.format("• %s: %d/%d unidades\n", 
                    rs.getString("Name"),
                    rs.getInt("Stock"),
                    rs.getInt("ReorderLevel")));
            }
            
            if (sb.toString().equals("⚠️ Productos con stock bajo:\n\n")) {
                sb.append("✅ No hay productos con stock bajo");
            }
            
            JOptionPane.showMessageDialog(this, sb.toString(), "Stock Bajo", JOptionPane.WARNING_MESSAGE);
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error cargando stock bajo: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}