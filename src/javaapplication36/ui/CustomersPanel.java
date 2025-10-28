package javaapplication36.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class CustomersPanel extends JPanel {
    private JTable customersTable;
    private DefaultTableModel customersModel;
    private Connection connection;
    private JTextField txtSearch;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh, btnNewPurchase, btnSearch;

    public CustomersPanel(Connection connection) {
        this.connection = connection;
        initializeUI();
        loadCustomers();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Modelo de la tabla - AJUSTADO para proveedores
        customersModel = new DefaultTableModel(new Object[]{
            "ID", "Empresa", "Contacto", "Teléfono", "Email", "Compras", "Fecha Registro"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Panel de búsqueda
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("🔍 Búsqueda de Proveedores"));
        
        txtSearch = new JTextField(20);
        btnSearch = new JButton("Buscar"); // DECLARADO CORRECTAMENTE
        btnRefresh = new JButton("🔄 Actualizar");
        
        // LISTENERS EN ORDEN CORRECTO
        btnSearch.addActionListener(e -> searchCustomers());
        btnRefresh.addActionListener(e -> loadCustomers());
        
        searchPanel.add(new JLabel("Buscar:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        // Tabla de proveedores
        customersTable = new JTable(customersModel);
        customersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(customersTable);

        // Panel de botones - AGREGADO BOTÓN DE COMPRA
        JPanel buttonPanel = new JPanel(new FlowLayout());
        btnAdd = new JButton("➕ Agregar Proveedor");
        btnEdit = new JButton("✏️ Editar Proveedor");
        btnDelete = new JButton("🗑️ Eliminar Proveedor");
        btnNewPurchase = new JButton("🛒 Nueva Compra");

        // LISTENERS EN ORDEN CORRECTO
        btnAdd.addActionListener(e -> addCustomer());
        btnEdit.addActionListener(e -> editCustomer());
        btnDelete.addActionListener(e -> deleteCustomer());
        btnNewPurchase.addActionListener(e -> newPurchase());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnNewPurchase);

        // Agregar componentes al panel principal
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadCustomers() {
    try {
        String sql = "SELECT c.CustomerId, c.Name, c.Phone, c.Email, c.CreatedAt, " +
                    "(SELECT COUNT(*) FROM InventoryMovements im " +
                    " WHERE im.MovementType = 'IN' " +
                    " AND im.Reference LIKE '%' + c.Name + '%') as TotalCompras " +
                    "FROM Customers c " +
                    "ORDER BY c.Name";
        
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        customersModel.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        
        while (rs.next()) {
            customersModel.addRow(new Object[]{
                rs.getInt("CustomerId"),
                rs.getString("Name"),
                "N/A",
                rs.getString("Phone"),
                rs.getString("Email"),
                rs.getInt("TotalCompras"), // Compras REALES del proveedor
                dateFormat.format(rs.getDate("CreatedAt"))
            });
        }
        
        rs.close();
        stmt.close();
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, 
            "Error cargando proveedores: " + e.getMessage(), 
            "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}
   private void searchCustomers() {
    String searchTerm = txtSearch.getText().trim();
    if (searchTerm.isEmpty()) {
        loadCustomers();
        return;
    }

    try {
        String sql = "SELECT c.CustomerId, c.Name, c.Phone, c.Email, c.CreatedAt, " +
                    "(SELECT COUNT(*) FROM InventoryMovements im " +
                    " WHERE im.MovementType = 'IN' " +
                    " AND im.Reference LIKE '%' + c.Name + '%') as TotalCompras " +  // ← CORREGIDO
                    "FROM Customers c " +
                    "WHERE c.Name LIKE ? OR c.Email LIKE ? OR c.Phone LIKE ? " +
                    "ORDER BY c.Name";  // ← Quitado el GROUP BY ya que no hay JOIN
        
        PreparedStatement stmt = connection.prepareStatement(sql);
        String likeTerm = "%" + searchTerm + "%";
        stmt.setString(1, likeTerm);
        stmt.setString(2, likeTerm);
        stmt.setString(3, likeTerm);
        
        ResultSet rs = stmt.executeQuery();
        customersModel.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        
        while (rs.next()) {
            customersModel.addRow(new Object[]{
                rs.getInt("CustomerId"),
                rs.getString("Name"),
                "N/A",
                rs.getString("Phone"),
                rs.getString("Email"),
                rs.getInt("TotalCompras"),  // Ahora mostrará compras reales
                dateFormat.format(rs.getDate("CreatedAt"))
            });
        }
        
        rs.close();
        stmt.close();
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, 
            "Error buscando proveedores: " + e.getMessage(), 
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}
    private void addCustomer() {
        // Diálogo para agregar proveedor
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        JTextField txtName = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtPhone = new JTextField();
        
        panel.add(new JLabel("Empresa*:"));
        panel.add(txtName);
        panel.add(new JLabel("Email:"));
        panel.add(txtEmail);
        panel.add(new JLabel("Teléfono:"));
        panel.add(txtPhone);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Agregar Nuevo Proveedor", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            if (txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre de la empresa es obligatorio");
                return;
            }
            
            try {
                String sql = "INSERT INTO Customers (Name, Email, Phone) VALUES (?, ?, ?)";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, txtName.getText().trim());
                stmt.setString(2, txtEmail.getText().trim());
                stmt.setString(3, txtPhone.getText().trim());
                
                stmt.executeUpdate();
                stmt.close();
                
                JOptionPane.showMessageDialog(this, "Proveedor agregado exitosamente");
                loadCustomers();
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error agregando proveedor: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editCustomer() {
        int selectedRow = customersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un proveedor para editar");
            return;
        }
        
        int customerId = (int) customersModel.getValueAt(selectedRow, 0);
        String currentName = (String) customersModel.getValueAt(selectedRow, 1);
        String currentPhone = (String) customersModel.getValueAt(selectedRow, 3);
        String currentEmail = (String) customersModel.getValueAt(selectedRow, 4);
        
        // Diálogo para editar proveedor
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        JTextField txtName = new JTextField(currentName);
        JTextField txtPhone = new JTextField(currentPhone);
        JTextField txtEmail = new JTextField(currentEmail);
        
        panel.add(new JLabel("Empresa*:"));
        panel.add(txtName);
        panel.add(new JLabel("Teléfono:"));
        panel.add(txtPhone);
        panel.add(new JLabel("Email:"));
        panel.add(txtEmail);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Editar Proveedor", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            if (txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre de la empresa es obligatorio");
                return;
            }
            
            try {
                String sql = "UPDATE Customers SET Name = ?, Email = ?, Phone = ? WHERE CustomerId = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, txtName.getText().trim());
                stmt.setString(2, txtEmail.getText().trim());
                stmt.setString(3, txtPhone.getText().trim());
                stmt.setInt(4, customerId);
                
                int affected = stmt.executeUpdate();
                stmt.close();
                
                if (affected > 0) {
                    JOptionPane.showMessageDialog(this, "Proveedor actualizado exitosamente");
                    loadCustomers();
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error actualizando proveedor: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteCustomer() {
        int selectedRow = customersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un proveedor para eliminar");
            return;
        }
        
        int customerId = (int) customersModel.getValueAt(selectedRow, 0);
        String customerName = (String) customersModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro de eliminar al proveedor: " + customerName + "?",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Verificar si el proveedor tiene ventas asociadas
                String checkSql = "SELECT COUNT(*) as ventas FROM Sales WHERE CustomerId = ?";
                PreparedStatement checkStmt = connection.prepareStatement(checkSql);
                checkStmt.setInt(1, customerId);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next() && rs.getInt("ventas") > 0) {
                    JOptionPane.showMessageDialog(this, 
                        "No se puede eliminar el proveedor porque tiene compras asociadas",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Eliminar proveedor
                String sql = "DELETE FROM Customers WHERE CustomerId = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, customerId);
                
                int affected = stmt.executeUpdate();
                stmt.close();
                
                if (affected > 0) {
                    JOptionPane.showMessageDialog(this, "Proveedor eliminado exitosamente");
                    loadCustomers();
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error eliminando proveedor: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void newPurchase() {
        int selectedRow = customersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un proveedor para registrar compra");
            return;
        }
        
        int supplierId = (int) customersModel.getValueAt(selectedRow, 0);
        String supplierName = (String) customersModel.getValueAt(selectedRow, 1);
        
        // Abrir diálogo para nueva compra
        openPurchaseDialog(supplierId, supplierName);
    }

    private void openPurchaseDialog(int supplierId, String supplierName) {
        // Crear diálogo de compra
        JDialog purchaseDialog = new JDialog();
        purchaseDialog.setTitle("🛒 Nueva Compra - " + supplierName);
        purchaseDialog.setSize(500, 400);
        purchaseDialog.setLayout(new BorderLayout());
        purchaseDialog.setLocationRelativeTo(this);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de información del proveedor
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Proveedor"));
        infoPanel.add(new JLabel("Empresa: " + supplierName));
        infoPanel.add(new JLabel("ID: " + supplierId));
        
        // Panel de productos disponibles
        JPanel productsPanel = new JPanel(new BorderLayout());
        productsPanel.setBorder(BorderFactory.createTitledBorder("Productos Disponibles para Compra"));
        
        DefaultTableModel productsModel = new DefaultTableModel(new Object[]{
            "ID", "Producto", "Precio Compra", "Stock Actual"
        }, 0);
        
        JTable productsTable = new JTable(productsModel);
        JScrollPane productsScroll = new JScrollPane(productsTable);
        
        // Cargar productos
        loadProductsForPurchase(productsModel);
        
        // Panel de compra
        JPanel purchasePanel = new JPanel(new GridLayout(4, 2, 5, 5));
        purchasePanel.setBorder(BorderFactory.createTitledBorder("Detalles de la Compra"));
        
        JComboBox<String> cmbProduct = new JComboBox<>();
        JTextField txtQuantity = new JTextField();
        JTextField txtUnitPrice = new JTextField();
        JTextField txtNotes = new JTextField("Compra a " + supplierName);
        
        // Cargar productos en el combobox
        loadProductsComboBox(cmbProduct);
        
        purchasePanel.add(new JLabel("Producto:"));
        purchasePanel.add(cmbProduct);
        purchasePanel.add(new JLabel("Cantidad:"));
        purchasePanel.add(txtQuantity);
        purchasePanel.add(new JLabel("Precio Unitario:"));
        purchasePanel.add(txtUnitPrice);
        purchasePanel.add(new JLabel("Notas:"));
        purchasePanel.add(txtNotes);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnProcessPurchase = new JButton("✅ Procesar Compra");
        JButton btnCancel = new JButton("❌ Cancelar");
        
        btnProcessPurchase.addActionListener(e -> {
            processPurchase(supplierId, supplierName, cmbProduct, txtQuantity, txtUnitPrice, txtNotes);
            purchaseDialog.dispose();
        });
        
        btnCancel.addActionListener(e -> purchaseDialog.dispose());
        
        buttonPanel.add(btnProcessPurchase);
        buttonPanel.add(btnCancel);

        // Agregar componentes al diálogo
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(productsScroll, BorderLayout.CENTER);
        mainPanel.add(purchasePanel, BorderLayout.SOUTH);
        
        purchaseDialog.add(mainPanel, BorderLayout.CENTER);
        purchaseDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        purchaseDialog.setVisible(true);
    }

    private void loadProductsForPurchase(DefaultTableModel productsModel) {
        try {
String sql = "SELECT ProductId, Name, CostPrice, Stock FROM Products WHERE IsActive = 1 ORDER BY Name";            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            productsModel.setRowCount(0);
            
            while (rs.next()) {
                productsModel.addRow(new Object[]{
                    rs.getInt("ProductId"),
                    rs.getString("Name"),
                    String.format("$%.2f", rs.getDouble("CostPrice")),
                    rs.getInt("Stock")
                });
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadProductsComboBox(JComboBox<String> cmbProduct) {
        try {
            String sql = "SELECT ProductId, Name FROM Products WHERE IsActive = 1 ORDER BY Name";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            cmbProduct.removeAllItems();
            
            while (rs.next()) {
                cmbProduct.addItem(rs.getString("Name"));
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void processPurchase(int supplierId, String supplierName, 
                               JComboBox<String> cmbProduct, JTextField txtQuantity, 
                               JTextField txtUnitPrice, JTextField txtNotes) {
        
        if (cmbProduct.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto");
            return;
        }
        
        String productName = cmbProduct.getSelectedItem().toString();
        String quantityStr = txtQuantity.getText().trim();
        String unitPriceStr = txtUnitPrice.getText().trim();
        String notes = txtNotes.getText().trim();
        
        if (quantityStr.isEmpty() || unitPriceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete cantidad y precio unitario");
            return;
        }
        
        try {
            int quantity = Integer.parseInt(quantityStr);
            double unitPrice = Double.parseDouble(unitPriceStr.replace("$", ""));
            
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0");
                return;
            }
            
            // Obtener ID del producto
            int productId = getProductIdByName(productName);
            
            if (productId > 0) {
                // CORREGIDO: Cambiado de ProducId a ProductId
                String sqlMovement = "INSERT INTO InventoryMovements (ProductId, MovementType, Quantity, UserId, Notes, Reference) " +
                                   "VALUES (?, 'IN', ?, ?, ?, ?)";
                PreparedStatement stmtMovement = connection.prepareStatement(sqlMovement);
                stmtMovement.setInt(1, productId);
                stmtMovement.setInt(2, quantity);
                stmtMovement.setInt(3, getCurrentUserId());
                stmtMovement.setString(4, notes);
                stmtMovement.setString(5, "Compra a " + supplierName);
                
                stmtMovement.executeUpdate();
                stmtMovement.close();
                
                // Actualizar stock del producto
                String sqlUpdateStock = "UPDATE Products SET Stock = Stock + ? WHERE ProductId = ?";
                PreparedStatement stmtStock = connection.prepareStatement(sqlUpdateStock);
                stmtStock.setInt(1, quantity);
                stmtStock.setInt(2, productId);
                stmtStock.executeUpdate();
                stmtStock.close();
                
                // Opcional: Actualizar precio de compra si es diferente
                if (unitPrice > 0) {
String sqlUpdatePrice = "UPDATE Products SET CostPrice = ? WHERE ProductId = ?";
PreparedStatement stmtPrice = connection.prepareStatement(sqlUpdatePrice);
                    stmtPrice.setDouble(1, unitPrice);
                    stmtPrice.setInt(2, productId);
                    stmtPrice.executeUpdate();
                    stmtPrice.close();
                }
                
                JOptionPane.showMessageDialog(this, 
                    "✅ Compra registrada exitosamente\n\n" +
                    "Proveedor: " + supplierName + "\n" +
                    "Producto: " + productName + "\n" +
                    "Cantidad: " + quantity + "\n" +
                    "Total: $" + (quantity * unitPrice),
                    "Compra Exitosa", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Actualizar la tabla de proveedores
                loadCustomers();
                
            } else {
                JOptionPane.showMessageDialog(this, "Producto no encontrado");
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Cantidad y precio deben ser números válidos");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error registrando compra: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
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
        // Por ahora retornamos 1 (admin)
        return 1;
    }
}