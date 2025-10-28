package javaapplication36.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class MovementsPanel extends JPanel {
    private JTable movementsTable;
    private DefaultTableModel movementsModel;
    private Connection connection;
    private JComboBox<String> cmbMovementType;
    private JComboBox<String> cmbProductFilter;
    private String productIdColumnName;

    public MovementsPanel(Connection connection) {
        this.connection = connection;
        this.productIdColumnName = detectProductIdColumnName();
        initializeUI();
        loadMovements();
        loadProductsFilter();
    }

    private String detectProductIdColumnName() {
        try {
            // Método 1: Consultar INFORMATION_SCHEMA
            String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_NAME = 'InventoryMovements' " +
                        "AND (COLUMN_NAME LIKE '%Product%' OR COLUMN_NAME LIKE '%Produc%')";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                System.out.println("Columna detectada via INFORMATION_SCHEMA: " + columnName);
                rs.close();
                stmt.close();
                return columnName;
            }
            rs.close();
            stmt.close();
            
            // Método 2: Usar DatabaseMetaData
            DatabaseMetaData metaData = connection.getMetaData();
            rs = metaData.getColumns(null, null, "InventoryMovements", null);
            
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                if (columnName.toLowerCase().contains("product") || 
                    columnName.toLowerCase().contains("produc")) {
                    System.out.println("Columna detectada via DatabaseMetaData: " + columnName);
                    rs.close();
                    return columnName;
                }
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error detectando columna: " + e.getMessage());
        }
        
        // Fallback
        System.out.println("Usando fallback: ProductId");
        return "ProductId";
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        movementsModel = new DefaultTableModel(new Object[]{
            "ID", "Fecha", "Producto", "Tipo", "Cantidad", "Usuario", "Notas", "Referencia"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("🔍 Filtros de Movimientos"));
        
        cmbMovementType = new JComboBox<>(new String[]{"Todos", "IN", "OUT", "AJUSTE"});
        cmbProductFilter = new JComboBox<>();
        cmbProductFilter.addItem("Todos los productos");
        
        JButton btnFilter = new JButton("Filtrar");
        JButton btnRefresh = new JButton("🔄 Actualizar");
        
        btnFilter.addActionListener(e -> applyFilters());
        btnRefresh.addActionListener(e -> loadMovements());
        
        filterPanel.add(new JLabel("Tipo:"));
        filterPanel.add(cmbMovementType);
        filterPanel.add(new JLabel("Producto:"));
        filterPanel.add(cmbProductFilter);
        filterPanel.add(btnFilter);
        filterPanel.add(btnRefresh);

        movementsTable = new JTable(movementsModel);
        movementsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(movementsTable);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnAddMovement = new JButton("➕ Nuevo Movimiento");
        JButton btnExport = new JButton("📊 Exportar Reporte");

        btnAddMovement.addActionListener(e -> addMovement());
        btnExport.addActionListener(e -> exportReport());

        buttonPanel.add(btnAddMovement);
        buttonPanel.add(btnExport);

        add(filterPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

   private void loadMovements() {
    // Resetear los filtros a "Todos"
    cmbMovementType.setSelectedItem("Todos");
    cmbProductFilter.setSelectedItem("Todos los productos");
    
    // Llamar a applyFilters que ahora carga todos cuando no hay filtros
    applyFilters();
}
    private void loadProductsFilter() {
        try {
            String sql = "SELECT ProductId, Name FROM Products WHERE IsActive = 1 ORDER BY Name";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            cmbProductFilter.removeAllItems();
            cmbProductFilter.addItem("Todos los productos");
            
            while (rs.next()) {
                cmbProductFilter.addItem(rs.getString("Name"));
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

   private void applyFilters() {
    try {
        String movementType = cmbMovementType.getSelectedItem().toString();
        String productFilter = cmbProductFilter.getSelectedItem().toString();
        
        StringBuilder sql = new StringBuilder(
            "SELECT m.MovementId, m.CreatedAt, p.Name as ProductName, " +
            "m.MovementType, m.Quantity, u.Username, m.Notes, m.Reference " +
            "FROM InventoryMovements m " +
            "LEFT JOIN Products p ON m." + productIdColumnName + " = p.ProductId " +
            "LEFT JOIN Users u ON m.UserId = u.UserId " +
            "WHERE 1=1"
        );
        
        // Aplicar filtro por tipo de movimiento
        if (!movementType.equals("Todos")) {
            sql.append(" AND m.MovementType = ?");
        }
        
        // Aplicar filtro por producto
        if (!productFilter.equals("Todos los productos")) {
            sql.append(" AND p.Name = ?");
        }
        
        sql.append(" ORDER BY m.CreatedAt DESC");
        
        PreparedStatement stmt = connection.prepareStatement(sql.toString());
        
        int paramIndex = 1;
        
        // Establecer parámetros para el tipo de movimiento
        if (!movementType.equals("Todos")) {
            stmt.setString(paramIndex++, movementType);
        }
        
        // Establecer parámetros para el producto
        if (!productFilter.equals("Todos los productos")) {
            stmt.setString(paramIndex++, productFilter);
        }
        
        System.out.println("Query con filtros: " + sql.toString());
        
        ResultSet rs = stmt.executeQuery();
        
        movementsModel.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        int rowCount = 0;
        while (rs.next()) {
            movementsModel.addRow(new Object[]{
                rs.getInt("MovementId"),
                dateFormat.format(rs.getTimestamp("CreatedAt")),
                rs.getString("ProductName"),
                rs.getString("MovementType"),
                rs.getInt("Quantity"),
                rs.getString("Username"),
                rs.getString("Notes"),
                rs.getString("Reference")
            });
            rowCount++;
        }
        
        System.out.println("Movimientos filtrados: " + rowCount);
        
        rs.close();
        stmt.close();
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, 
            "Error aplicando filtros: " + e.getMessage(), 
            "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}

    private void addMovement() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
        
        JComboBox<String> cmbProduct = new JComboBox<>();
        JComboBox<String> cmbType = new JComboBox<>(new String[]{"IN", "OUT", "AJUSTE"});
        JTextField txtQuantity = new JTextField();
        JTextField txtNotes = new JTextField();
        JTextField txtReference = new JTextField();
        
        try {
            String sql = "SELECT ProductId, Name FROM Products WHERE IsActive = 1 ORDER BY Name";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                cmbProduct.addItem(rs.getString("Name"));
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        panel.add(new JLabel("Producto:"));
        panel.add(cmbProduct);
        panel.add(new JLabel("Tipo:"));
        panel.add(cmbType);
        panel.add(new JLabel("Cantidad:"));
        panel.add(txtQuantity);
        panel.add(new JLabel("Notas:"));
        panel.add(txtNotes);
        panel.add(new JLabel("Referencia:"));
        panel.add(txtReference);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Nuevo Movimiento de Inventario", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            if (txtQuantity.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "La cantidad es obligatoria");
                return;
            }
            
            try {
                int quantity = Integer.parseInt(txtQuantity.getText().trim());
                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0");
                    return;
                }
                
                int productId = getProductIdByName(cmbProduct.getSelectedItem().toString());
                
                if (productId > 0) {
                    String sql = "INSERT INTO InventoryMovements (" + productIdColumnName + ", MovementType, Quantity, UserId, Notes, Reference) " +
                                "VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt = connection.prepareStatement(sql);
                    stmt.setInt(1, productId);
                    stmt.setString(2, cmbType.getSelectedItem().toString());
                    stmt.setInt(3, quantity);
                    stmt.setInt(4, getCurrentUserId());
                    stmt.setString(5, txtNotes.getText().trim());
                    stmt.setString(6, txtReference.getText().trim());
                    
                    stmt.executeUpdate();
                    stmt.close();
                    
                    updateProductStock(productId, cmbType.getSelectedItem().toString(), quantity);
                    
                    JOptionPane.showMessageDialog(this, "Movimiento registrado exitosamente");
                    loadMovements();
                }
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser un número válido");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error registrando movimiento: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private int getProductIdByName(String productName) {
        try {
            String sql = "SELECT ProductId FROM Products WHERE Name = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, productName);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("ProductId");
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getCurrentUserId() {
        return 1;
    }

    private void updateProductStock(int productId, String movementType, int quantity) throws SQLException {
        String sql;
        if ("IN".equals(movementType)) {
            sql = "UPDATE Products SET Stock = Stock + ? WHERE ProductId = ?";
        } else {
            sql = "UPDATE Products SET Stock = Stock - ? WHERE ProductId = ?";
        }
        
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, quantity);
        stmt.setInt(2, productId);
        stmt.executeUpdate();
        stmt.close();
    }

    private void exportReport() {
        JOptionPane.showMessageDialog(this, 
            "Función de exportación en desarrollo...\nSe exportará a Excel o PDF",
            "Exportar Reporte", 
            JOptionPane.INFORMATION_MESSAGE);
    }
}