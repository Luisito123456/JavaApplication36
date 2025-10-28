package javaapplication36.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class SalesPanel extends JPanel {
    private JTable salesTable, productsTable, cartTable;
    private DefaultTableModel salesModel, productsModel, cartModel;
    private Connection connection;
    private String currentUser;
    private JLabel lblTotal, lblStats;
    private int currentSaleId = 0;
    
    // Mapa para manejar items del carrito
    private Map<Integer, CartItem> cartItems = new HashMap<>();

    public SalesPanel(Connection connection, String username) {
        this.connection = connection;
        this.currentUser = username;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Inicializar los modelos PRIMERO
        salesModel = new DefaultTableModel(new Object[]{
            "ID Venta", "Cliente", "Vendedor", "Fecha", "Total", "Método Pago"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        productsModel = new DefaultTableModel(new Object[]{
            "ID", "SKU", "Producto", "Precio", "Stock"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        cartModel = new DefaultTableModel(new Object[]{
            "Producto", "Precio", "Cantidad", "Subtotal"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Panel de estadísticas
        lblStats = new JLabel("Cargando estadísticas de ventas...");
        lblStats.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        lblStats.setFont(new Font("Arial", Font.BOLD, 12));
        add(lblStats, BorderLayout.NORTH);

        // Panel principal dividido
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplit.setDividerLocation(300);

        // Panel superior - Ventas registradas
        JPanel salesPanel = new JPanel(new BorderLayout());
        salesPanel.setBorder(BorderFactory.createTitledBorder("📋 Historial de Ventas"));
        
        salesTable = new JTable(salesModel);
        salesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        salesPanel.add(new JScrollPane(salesTable), BorderLayout.CENTER);

        // Panel inferior dividido en productos y carrito
        JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        bottomSplit.setDividerLocation(600);

        // Panel izquierdo - Productos disponibles
        JPanel productsPanel = new JPanel(new BorderLayout());
        productsPanel.setBorder(BorderFactory.createTitledBorder("🍷 Productos Disponibles"));
        
        productsTable = new JTable(productsModel);
        productsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Doble click para agregar al carrito
        productsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addToCart();
                }
            }
        });
        
        // Botón para agregar al carrito
        JPanel productsButtonPanel = new JPanel(new FlowLayout());
        JButton btnAddToCart = new JButton("➕ Agregar al Carrito");
        btnAddToCart.addActionListener(e -> addToCart());
        productsButtonPanel.add(btnAddToCart);
        
        productsPanel.add(new JScrollPane(productsTable), BorderLayout.CENTER);
        productsPanel.add(productsButtonPanel, BorderLayout.SOUTH);

        // Panel derecho - Carrito de venta
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBorder(BorderFactory.createTitledBorder("🛒 Carrito de Venta"));
        
        cartTable = new JTable(cartModel);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Doble click para editar cantidad
        cartTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editQuantity();
                }
            }
        });
        
        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        // Panel de total y botones del carrito
        JPanel cartBottomPanel = new JPanel(new BorderLayout());
        
        lblTotal = new JLabel("TOTAL: $0.00", JLabel.CENTER);
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotal.setForeground(new Color(44, 62, 80));
        lblTotal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel cartButtons = new JPanel(new FlowLayout());
        JButton btnProcessSale = new JButton("✅ Procesar Venta");
        JButton btnCancelSale = new JButton("❌ Cancelar");
        JButton btnRemoveItem = new JButton("🗑️ Quitar Item");
        
        btnProcessSale.addActionListener(e -> processSale());
        btnCancelSale.addActionListener(e -> clearCart());
        btnRemoveItem.addActionListener(e -> removeSelectedItem());
        
        cartButtons.add(btnProcessSale);
        cartButtons.add(btnCancelSale);
        cartButtons.add(btnRemoveItem);
        
        cartBottomPanel.add(lblTotal, BorderLayout.NORTH);
        cartBottomPanel.add(cartButtons, BorderLayout.SOUTH);
        
        cartPanel.add(cartBottomPanel, BorderLayout.SOUTH);

        bottomSplit.setLeftComponent(productsPanel);
        bottomSplit.setRightComponent(cartPanel);

        mainSplit.setTopComponent(salesPanel);
        mainSplit.setBottomComponent(bottomSplit);

        add(mainSplit, BorderLayout.CENTER);

        // Panel de botones de ventas
        JPanel salesButtons = new JPanel(new FlowLayout());
        JButton btnRefreshSales = new JButton("🔄 Actualizar Ventas");
        JButton btnNewSale = new JButton("➕ Nueva Venta");
        
        btnRefreshSales.addActionListener(e -> loadSales());
        btnNewSale.addActionListener(e -> clearCart());
        
        salesButtons.add(btnRefreshSales);
        salesButtons.add(btnNewSale);
        add(salesButtons, BorderLayout.SOUTH);

        // Cargar datos DESPUÉS de inicializar la UI
        loadSales();
        loadProducts();
    }

    private void loadSales() {
        try {
            String sql = "SELECT s.SaleId, c.Name, u.Username, " +
                        "s.SaleDate, s.TotalAmount, s.PaymentMethod " +
                        "FROM Sales s " +
                        "LEFT JOIN Customers c ON s.CustomerId = c.CustomerId " +
                        "LEFT JOIN Users u ON s.UserId = u.UserId " +
                        "ORDER BY s.SaleDate DESC";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            salesModel.setRowCount(0);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            while (rs.next()) {
                salesModel.addRow(new Object[]{
                    rs.getInt("SaleId"),
                    rs.getString("Name"),
                    rs.getString("Username"),
                    dateFormat.format(rs.getTimestamp("SaleDate")),
                    String.format("$%.2f", rs.getDouble("TotalAmount")),
                    rs.getString("PaymentMethod")
                });
            }
            
            rs.close();
            stmt.close();
            
            updateSalesStatistics();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error cargando ventas: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProducts() {
        try {
            String sql = "SELECT ProductId, SKU, Name, SalePrice, Stock " +
                        "FROM Products WHERE IsActive = 1 AND Stock > 0 " +
                        "ORDER BY Name";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            productsModel.setRowCount(0);
            
            while (rs.next()) {
                productsModel.addRow(new Object[]{
                    rs.getInt("ProductId"),
                    rs.getString("SKU"),
                    rs.getString("Name"),
                    String.format("$%.2f", rs.getDouble("SalePrice")),
                    rs.getInt("Stock")
                });
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error cargando productos: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addToCart() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto");
            return;
        }
        
        int productId = (int) productsModel.getValueAt(selectedRow, 0);
        String productName = (String) productsModel.getValueAt(selectedRow, 2);
        double price = Double.parseDouble(((String) productsModel.getValueAt(selectedRow, 3)).replace("$", ""));
        int stock = (int) productsModel.getValueAt(selectedRow, 4);
        
        // Verificar stock
        if (stock < 1) {
            JOptionPane.showMessageDialog(this, "Stock insuficiente para " + productName);
            return;
        }
        
        // Si ya está en el carrito, incrementar cantidad
        if (cartItems.containsKey(productId)) {
            CartItem item = cartItems.get(productId);
            if (item.getQuantity() + 1 > stock) {
                JOptionPane.showMessageDialog(this, "Stock máximo alcanzado para " + productName);
                return;
            }
            item.setQuantity(item.getQuantity() + 1);
        } else {
            // Agregar nuevo item al carrito
            cartItems.put(productId, new CartItem(productId, productName, price, 1, stock));
        }
        
        refreshCartTable();
        updateTotal();
    }

    private void refreshCartTable() {
        cartModel.setRowCount(0);
        for (CartItem item : cartItems.values()) {
            cartModel.addRow(new Object[]{
                item.getProductName(),
                item.getPrice(),
                item.getQuantity(),
                item.getSubtotal()
            });
        }
    }

    private void removeSelectedItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un item para remover");
            return;
        }
        
        String productName = (String) cartModel.getValueAt(selectedRow, 0);
        
        // Encontrar y remover del mapa
        cartItems.entrySet().removeIf(entry -> entry.getValue().getProductName().equals(productName));
        
        refreshCartTable();
        updateTotal();
    }

    private void editQuantity() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        String productName = (String) cartModel.getValueAt(selectedRow, 0);
        
        // Encontrar el item
        CartItem item = cartItems.values().stream()
            .filter(cartItem -> cartItem.getProductName().equals(productName))
            .findFirst()
            .orElse(null);
        
        if (item != null) {
            String newQtyStr = JOptionPane.showInputDialog(this, 
                "Nueva cantidad para " + productName + " (Stock disponible: " + item.getStock() + "):", 
                item.getQuantity());
            
            if (newQtyStr != null) {
                try {
                    int newQty = Integer.parseInt(newQtyStr);
                    if (newQty > 0 && newQty <= item.getStock()) {
                        item.setQuantity(newQty);
                        refreshCartTable();
                        updateTotal();
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "Cantidad inválida. Debe ser entre 1 y " + item.getStock());
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Ingrese un número válido");
                }
            }
        }
    }

    private void clearCart() {
        cartItems.clear();
        cartModel.setRowCount(0);
        updateTotal();
        JOptionPane.showMessageDialog(this, "Carrito limpiado");
    }

    private void updateTotal() {
        double total = 0;
        for (CartItem item : cartItems.values()) {
            total += item.getSubtotal();
        }
        lblTotal.setText(String.format("TOTAL: $%.2f", total));
    }

    private void processSale() {
    if (cartItems.isEmpty()) {
        JOptionPane.showMessageDialog(this, "El carrito está vacío");
        return;
    }
    
    // Diálogo para método de pago
    String[] paymentMethods = {"Efectivo", "Tarjeta Débito", "Tarjeta Crédito", "Transferencia"};
    String paymentMethod = (String) JOptionPane.showInputDialog(
        this, "Seleccione método de pago:", "Método de Pago",
        JOptionPane.QUESTION_MESSAGE, null, paymentMethods, paymentMethods[0]
    );
    
    if (paymentMethod == null) return;
    
    try {
        connection.setAutoCommit(false);
        
        // 1. Crear la venta
        String sqlSale = "INSERT INTO Sales (CustomerId, TotalAmount, PaymentMethod, UserId) VALUES (?, ?, ?, ?)";
        PreparedStatement stmtSale = connection.prepareStatement(sqlSale, Statement.RETURN_GENERATED_KEYS);
        stmtSale.setInt(1, 1); // Cliente mostrador
        stmtSale.setDouble(2, getTotalAmount());
        stmtSale.setString(3, paymentMethod);
        stmtSale.setInt(4, getCurrentUserId());
        
        stmtSale.executeUpdate();
        
        // Obtener ID de la venta
        ResultSet rs = stmtSale.getGeneratedKeys();
        int saleId = 0;
        if (rs.next()) {
            saleId = rs.getInt(1);
            currentSaleId = saleId;
        }
        rs.close();
        stmtSale.close();
        
        System.out.println("✅ Venta creada #" + saleId + " - Total: $" + getTotalAmount());
        
        // 2. Insertar items de venta y actualizar stock
        for (CartItem item : cartItems.values()) {
            System.out.println("Procesando item: " + item.getProductName() + 
                             " - Cantidad: " + item.getQuantity() + 
                             " - Precio: $" + item.getPrice());
            
            // Insertar en SaleItems (SOLO columnas no computadas)
            insertSaleItem(saleId, item);
            
            // Actualizar stock
            updateProductStock(item.getProductId(), item.getQuantity());
            
            // Registrar movimiento
            registerInventoryMovement(item.getProductId(), "OUT", item.getQuantity(), saleId);
        }
        
        connection.commit();
        
        JOptionPane.showMessageDialog(this, 
            String.format("✅ Venta #%d procesada exitosamente\n\nTotal: $%.2f\nMétodo: %s\nItems: %d", 
                saleId, getTotalAmount(), paymentMethod, cartItems.size()),
            "Venta Exitosa", 
            JOptionPane.INFORMATION_MESSAGE);
        
        clearCart();
        loadSales();
        loadProducts();
        
    } catch (SQLException e) {
        try {
            connection.rollback();
            System.err.println("❌ Error en transacción - Rollback ejecutado: " + e.getMessage());
        } catch (SQLException ex) {
            System.err.println("❌ Error haciendo rollback: " + ex.getMessage());
        }
        
        JOptionPane.showMessageDialog(this, 
            "Error procesando venta: " + e.getMessage(), 
            "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    } finally {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

    private double getTotalAmount() {
        double total = 0;
        for (CartItem item : cartItems.values()) {
            total += item.getSubtotal();
        }
        return total;
    }

    private int getCurrentUserId() {
        try {
            String sql = "SELECT UserId FROM Users WHERE Username = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, currentUser);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("UserId");
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1; // Fallback al admin
    }

private void insertSaleItem(int saleId, CartItem item) throws SQLException {
    // SOLO insertar las columnas que NO son computadas
    String sql = "INSERT INTO SaleItems (SaleId, ProductId, Quantity, UnitPrice) VALUES (?, ?, ?, ?)";
    PreparedStatement stmt = connection.prepareStatement(sql);
    stmt.setInt(1, saleId);
    stmt.setInt(2, item.getProductId());
    stmt.setInt(3, item.getQuantity());
    stmt.setDouble(4, item.getPrice());
    stmt.executeUpdate();
    stmt.close();
    System.out.println("✅ Item insertado en SaleItems: " + item.getProductName() + " - Cantidad: " + item.getQuantity());
}

   private void updateProductStock(int productId, int quantity) throws SQLException {
    String sql = "UPDATE Products SET Stock = Stock - ? WHERE ProductId = ?";
    PreparedStatement stmt = connection.prepareStatement(sql);
    stmt.setInt(1, quantity);
    stmt.setInt(2, productId);
    stmt.executeUpdate();
    stmt.close();
    System.out.println("✅ Stock actualizado - ProductId: " + productId + " - Cantidad: -" + quantity);
}

   private void registerInventoryMovement(int productId, String type, int quantity, int saleId) throws SQLException {
    try {
        String sqlMovement = "INSERT INTO InventoryMovements (ProductId, MovementType, Quantity, UserId, Notes) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmtMovement = connection.prepareStatement(sqlMovement);
        stmtMovement.setInt(1, productId);
        stmtMovement.setString(2, type);
        stmtMovement.setInt(3, quantity);
        stmtMovement.setInt(4, getCurrentUserId());
        stmtMovement.setString(5, "Venta #" + saleId);
        stmtMovement.executeUpdate();
        stmtMovement.close();
        System.out.println("✅ Movimiento registrado - ProductId: " + productId + " - Tipo: " + type + " - Cantidad: " + quantity);
    } catch (SQLException e) {
        System.err.println("❌ Error registrando movimiento: " + e.getMessage());
        throw e; // Relanzar la excepción
    }
}


    private void updateSalesStatistics() {
        try {
            // Ventas de hoy
            String sqlToday = "SELECT COUNT(*) as ventasHoy, COALESCE(SUM(TotalAmount), 0) as totalHoy FROM Sales WHERE CAST(SaleDate AS DATE) = CAST(CURRENT_DATE AS DATE)";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sqlToday);
            
            int ventasHoy = 0;
            double totalHoy = 0;
            
            if (rs.next()) {
                ventasHoy = rs.getInt("ventasHoy");
                totalHoy = rs.getDouble("totalHoy");
            }
            
            lblStats.setText(String.format(
                "💰 Ventas Hoy: %d | Total Hoy: $%.2f | Carrito: %d items | Total: $%.2f", 
                ventasHoy, totalHoy, cartItems.size(), getTotalAmount()
            ));
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            lblStats.setText("Estadísticas: Carrito: " + cartItems.size() + " items | Total: $" + getTotalAmount());
        }
    }

    // Clase interna para manejar items del carrito
    private class CartItem {
        private int productId;
        private String productName;
        private double price;
        private int quantity;
        private int stock;
        
        public CartItem(int productId, String productName, double price, int quantity, int stock) {
            this.productId = productId;
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
            this.stock = stock;
        }
        
        public double getSubtotal() { return price * quantity; }
        public int getProductId() { return productId; }
        public String getProductName() { return productName; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public int getStock() { return stock; }
    }
}