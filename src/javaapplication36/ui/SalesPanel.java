package javaapplication36.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import javaapplication36.util.PDFGenerator;

public class SalesPanel extends JPanel {
    private JTable salesTable, productsTable, cartTable;
    private DefaultTableModel salesModel, productsModel, cartModel;
    private Connection connection;
    private String currentUser;
    private JLabel lblTotal, lblStats;
    private int currentSaleId = 0;
    
    // Mapa para manejar items del carrito
    private Map<Integer, CartItem> cartItems = new HashMap<>();

    // Variables para datos del cliente
    private String clienteNombre = "";
    private String clienteApellidos = "";
    private String clienteDNI = "";
    private String clienteRUC = "";
    private String metodoPagoActual = "Efectivo";

    // === MÉTODO DIAGNÓSTICO ===
    private void diagnosticarProblemaFecha() {
        try {
            System.out.println("🔍 DIAGNÓSTICO DE FECHAS:");
            
            // Fecha sistema Java
            java.util.Date javaDate = new java.util.Date();
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            System.out.println("🖥️  FECHA SISTEMA JAVA: " + format.format(javaDate));
            
            // Fecha base de datos
            String sql = "SELECT GETDATE() as dbDate, @@SERVERNAME as serverName, @@LANGUAGE as language";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                System.out.println("🗄️  FECHA SQL SERVER: " + rs.getTimestamp("dbDate"));
                System.out.println("🔧 SERVIDOR: " + rs.getString("serverName"));
                System.out.println("🌐 IDIOMA: " + rs.getString("language"));
            }
            rs.close();
            stmt.close();
            
            // Verificar zona horaria
            try {
                String sqlTZ = "SELECT CURRENT_TIMEZONE() as timezone";
                Statement stmtTZ = connection.createStatement();
                ResultSet rsTZ = stmtTZ.executeQuery(sqlTZ);
                if (rsTZ.next()) {
                    System.out.println("⏰ ZONA HORARIA BD: " + rsTZ.getString("timezone"));
                }
                rsTZ.close();
                stmtTZ.close();
            } catch (SQLException e) {
                System.out.println("ℹ️  No se pudo obtener zona horaria (puede ser normal en algunas versiones)");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error en diagnóstico: " + e.getMessage());
        }
    }

    // CONSTRUCTOR
    public SalesPanel(Connection connection, String username) {
        this.connection = connection;
        this.currentUser = username;
        diagnosticarProblemaFecha();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Inicializar los modelos
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
        
        // Agregar botones para el historial
        JPanel salesButtonPanel = new JPanel(new FlowLayout());
        JButton btnGenerateInvoice = new JButton("🧾 Generar Factura/Boleta");
        JButton btnViewSaleDetails = new JButton("👁️ Ver Detalles Venta");
        
        btnGenerateInvoice.addActionListener(e -> generateInvoiceFromHistory());
        btnViewSaleDetails.addActionListener(e -> viewSaleDetails());
        
        salesButtonPanel.add(btnGenerateInvoice);
        salesButtonPanel.add(btnViewSaleDetails);
        
        salesPanel.add(new JScrollPane(salesTable), BorderLayout.CENTER);
        salesPanel.add(salesButtonPanel, BorderLayout.SOUTH);

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
        JButton btnCustomerData = new JButton("👤 Datos Cliente");
        
        btnProcessSale.addActionListener(e -> processSale());
        btnCancelSale.addActionListener(e -> clearCart());
        btnRemoveItem.addActionListener(e -> removeSelectedItem());
        btnCustomerData.addActionListener(e -> showCustomerDataDialog());
        
        cartButtons.add(btnProcessSale);
        cartButtons.add(btnCancelSale);
        cartButtons.add(btnRemoveItem);
        cartButtons.add(btnCustomerData);
        
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

        // Cargar datos
        loadSales();
        loadProducts();
    }

    // ==================== MÉTODOS PARA DATOS DEL CLIENTE ====================

    private void showCustomerDataDialog() {
        JDialog customerDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Datos del Cliente", true);
        customerDialog.setSize(400, 300);
        customerDialog.setLocationRelativeTo(this);
        customerDialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField txtNombre = new JTextField(clienteNombre);
        JTextField txtApellidos = new JTextField(clienteApellidos);
        JTextField txtDNI = new JTextField(clienteDNI);
        JTextField txtRUC = new JTextField(clienteRUC);

        formPanel.add(new JLabel("Nombre:"));
        formPanel.add(txtNombre);
        formPanel.add(new JLabel("Apellidos:"));
        formPanel.add(txtApellidos);
        formPanel.add(new JLabel("DNI:"));
        formPanel.add(txtDNI);
        formPanel.add(new JLabel("RUC:"));
        formPanel.add(txtRUC);
        formPanel.add(new JLabel(""));
        formPanel.add(new JLabel(""));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnGuardar = new JButton("💾 Guardar");
        JButton btnLimpiar = new JButton("🗑️ Limpiar");

        btnGuardar.addActionListener(e -> {
            clienteNombre = txtNombre.getText().trim();
            clienteApellidos = txtApellidos.getText().trim();
            clienteDNI = txtDNI.getText().trim();
            clienteRUC = txtRUC.getText().trim();
            customerDialog.dispose();
            JOptionPane.showMessageDialog(this, 
                "Datos del cliente guardados:\n" +
                "Nombre: " + clienteNombre + " " + clienteApellidos + "\n" +
                (clienteDNI.isEmpty() ? "" : "DNI: " + clienteDNI + "\n") +
                (clienteRUC.isEmpty() ? "" : "RUC: " + clienteRUC), 
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
        });

        btnLimpiar.addActionListener(e -> {
            txtNombre.setText("");
            txtApellidos.setText("");
            txtDNI.setText("");
            txtRUC.setText("");
        });

        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnLimpiar);

        customerDialog.add(formPanel, BorderLayout.CENTER);
        customerDialog.add(buttonPanel, BorderLayout.SOUTH);
        customerDialog.setVisible(true);
    }

    private String getClienteCompleto() {
        if (!clienteNombre.isEmpty() && !clienteApellidos.isEmpty()) {
            return clienteNombre + " " + clienteApellidos;
        } else if (!clienteNombre.isEmpty()) {
            return clienteNombre;
        } else if (!clienteApellidos.isEmpty()) {
            return clienteApellidos;
        } else if (!clienteDNI.isEmpty()) {
            return "DNI: " + clienteDNI;
        } else if (!clienteRUC.isEmpty()) {
            return "RUC: " + clienteRUC;
        } else {
            return "Cliente Mostrador";
        }
    }

    // ==================== MÉTODO PARA VER DETALLES DE VENTA ====================
    private void viewSaleDetails() {
        int selectedRow = salesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una venta del historial para ver los detalles");
            return;
        }

        int saleId = (int) salesModel.getValueAt(selectedRow, 0);
        String cliente = (String) salesModel.getValueAt(selectedRow, 1);
        String fecha = (String) salesModel.getValueAt(selectedRow, 3);
        String total = (String) salesModel.getValueAt(selectedRow, 4);
        String metodoPago = (String) salesModel.getValueAt(selectedRow, 5);

        try {
            // Obtener detalles de la venta
            String sql = "SELECT p.Name as Producto, si.Quantity as Cantidad, " +
                        "si.UnitPrice as Precio, (si.Quantity * si.UnitPrice) as Subtotal " +
                        "FROM SaleItems si " +
                        "INNER JOIN Products p ON si.ProductId = p.ProductId " +
                        "WHERE si.SaleId = ? " +
                        "ORDER BY p.Name";
            
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, saleId);
            ResultSet rs = stmt.executeQuery();

            // Crear modelo de tabla para los detalles
            DefaultTableModel detailsModel = new DefaultTableModel(
                new Object[]{"Producto", "Cantidad", "Precio Unitario", "Subtotal"}, 0
            );

            double totalVenta = 0;
            int totalItems = 0;
            
            while (rs.next()) {
                String producto = rs.getString("Producto");
                int cantidad = rs.getInt("Cantidad");
                double precio = rs.getDouble("Precio");
                double subtotal = rs.getDouble("Subtotal");
                totalVenta += subtotal;
                totalItems += cantidad;

                detailsModel.addRow(new Object[]{
                    producto,
                    cantidad,
                    String.format("$%.2f", precio),
                    String.format("$%.2f", subtotal)
                });
            }

            rs.close();
            stmt.close();

            // Crear diálogo de detalles
            JDialog detailsDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                               "Detalles de Venta #" + saleId, true);
            detailsDialog.setSize(600, 400);
            detailsDialog.setLocationRelativeTo(this);
            detailsDialog.setLayout(new BorderLayout());

            // Panel de información general
            JPanel infoPanel = new JPanel(new GridLayout(4, 2, 10, 5));
            infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            infoPanel.add(new JLabel("Cliente:"));
            infoPanel.add(new JLabel(cliente));
            infoPanel.add(new JLabel("Fecha:"));
            infoPanel.add(new JLabel(fecha));
            infoPanel.add(new JLabel("Método de Pago:"));
            infoPanel.add(new JLabel(metodoPago));
            infoPanel.add(new JLabel("Total:"));
            infoPanel.add(new JLabel(total));

            // Tabla de detalles
            JTable detailsTable = new JTable(detailsModel);
            detailsTable.setFillsViewportHeight(true);
            
            JScrollPane scrollPane = new JScrollPane(detailsTable);
            scrollPane.setBorder(BorderFactory.createTitledBorder("Productos Comprados (" + totalItems + " items)"));

            // Panel de resumen
            JPanel summaryPanel = new JPanel(new FlowLayout());
            summaryPanel.add(new JLabel("Total Productos: " + detailsModel.getRowCount() + " | " +
                                       "Total Items: " + totalItems + " | " +
                                       "Total Venta: " + total));

            // Botones
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton btnClose = new JButton("Cerrar");
            JButton btnGeneratePDF = new JButton("📄 Generar PDF");
            
            btnClose.addActionListener(e -> detailsDialog.dispose());
            btnGeneratePDF.addActionListener(e -> {
                detailsDialog.dispose();
                generateInvoiceFromHistory();
            });

            buttonPanel.add(btnClose);
            buttonPanel.add(btnGeneratePDF);

            // Agregar componentes al diálogo
            detailsDialog.add(infoPanel, BorderLayout.NORTH);
            detailsDialog.add(scrollPane, BorderLayout.CENTER);
            detailsDialog.add(summaryPanel, BorderLayout.SOUTH);
            detailsDialog.add(buttonPanel, BorderLayout.SOUTH);

            detailsDialog.setVisible(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error obteniendo detalles de venta: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== MÉTODO PARA GENERAR FACTURA DESDE HISTORIAL ====================
    private void generateInvoiceFromHistory() {
        int selectedRow = salesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una venta del historial para generar la factura");
            return;
        }

        int saleId = (int) salesModel.getValueAt(selectedRow, 0);
        String cliente = (String) salesModel.getValueAt(selectedRow, 1);
        String fecha = (String) salesModel.getValueAt(selectedRow, 3);
        String total = (String) salesModel.getValueAt(selectedRow, 4);
        String metodoPago = (String) salesModel.getValueAt(selectedRow, 5);

        try {
            // Obtener detalles de la venta
            String sql = "SELECT p.Name, si.Quantity, si.UnitPrice, (si.Quantity * si.UnitPrice) as Subtotal " +
                        "FROM SaleItems si " +
                        "INNER JOIN Products p ON si.ProductId = p.ProductId " +
                        "WHERE si.SaleId = ?";
            
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, saleId);
            ResultSet rs = stmt.executeQuery();

            StringBuilder invoiceContent = new StringBuilder();
            double totalVenta = 0;
            
            while (rs.next()) {
                String producto = rs.getString("Name");
                int cantidad = rs.getInt("Quantity");
                double precio = rs.getDouble("UnitPrice");
                double subtotal = rs.getDouble("Subtotal");
                totalVenta += subtotal;

                invoiceContent.append(String.format("%s|%d|%.2f|%.2f\n", 
                    producto, cantidad, precio, subtotal));
            }

            rs.close();
            stmt.close();

            // Generar PDF con diseño profesional
            String fileName = "factura_venta_" + saleId + "_" + 
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".pdf";
            
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new java.io.File(fileName));
            fileChooser.setDialogTitle("Guardar Factura como PDF");

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File selectedFile = fileChooser.getSelectedFile();
                if (!selectedFile.getName().toLowerCase().endsWith(".pdf")) {
                    selectedFile = new java.io.File(selectedFile.getAbsolutePath() + ".pdf");
                }

                boolean success = PDFGenerator.generateInvoicePDF(
                    invoiceContent.toString(), 
                    "FACTURA N°: " + saleId,
                    selectedFile.getAbsolutePath(),
                    cliente,
                    "", // DNI no disponible en historial
                    "", // RUC no disponible en historial
                    currentUser,
                    metodoPago
                );

                if (success) {
                    JOptionPane.showMessageDialog(this, 
                        "✅ Factura generada exitosamente:\n" + selectedFile.getName(), 
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Preguntar si desea abrir el PDF
                    int openFile = JOptionPane.showConfirmDialog(this,
                        "¿Desea abrir el archivo PDF ahora?",
                        "Abrir PDF",
                        JOptionPane.YES_NO_OPTION);
                    
                    if (openFile == JOptionPane.YES_OPTION) {
                        try {
                            java.awt.Desktop.getDesktop().open(selectedFile);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this,
                                "No se pudo abrir el archivo automáticamente",
                                "Información",
                                JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "❌ Error al generar la factura", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error generando factura: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== MÉTODO PRINCIPAL PARA PROCESAR VENTA ====================
    private void processSale() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío");
            return;
        }
        
        // Diálogo para método de pago
        String[] paymentMethods = {"Efectivo", "Tarjeta Débito", "Tarjeta Crédito", "Transferencia"};
        metodoPagoActual = (String) JOptionPane.showInputDialog(
            this, "Seleccione método de pago:", "Método de Pago",
            JOptionPane.QUESTION_MESSAGE, null, paymentMethods, paymentMethods[0]
        );
        
        if (metodoPagoActual == null) return;

        // Preguntar si desea generar factura/boleta
        int generarFactura = JOptionPane.showConfirmDialog(this,
            "¿Desea generar Factura/Boleta para esta venta?",
            "Generar Comprobante",
            JOptionPane.YES_NO_OPTION);

        try {
            connection.setAutoCommit(false);
            
            // USAR FECHA DEL SISTEMA JAVA, NO LA DE LA BD
            java.util.Date currentDate = new java.util.Date();
            java.sql.Timestamp saleTimestamp = new java.sql.Timestamp(currentDate.getTime());
            
            // 1. Crear la venta CON FECHA EXPLÍCITA
            String sqlSale = "INSERT INTO Sales (CustomerId, TotalAmount, PaymentMethod, UserId, ClienteNombre, SaleDate) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmtSale = connection.prepareStatement(sqlSale, Statement.RETURN_GENERATED_KEYS);
            stmtSale.setInt(1, 1); // Cliente mostrador
            stmtSale.setDouble(2, getTotalAmount());
            stmtSale.setString(3, metodoPagoActual);
            stmtSale.setInt(4, getCurrentUserId());
            stmtSale.setString(5, getClienteCompleto());
            stmtSale.setTimestamp(6, saleTimestamp); // FECHA EXPLÍCITA
            
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
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            System.out.println("✅ Venta creada #" + saleId + " - Fecha: " + dateFormat.format(currentDate) + " - Cliente: " + getClienteCompleto() + " - Total: $" + getTotalAmount());
            
            // 2. Insertar items de venta y actualizar stock
            for (CartItem item : cartItems.values()) {
                System.out.println("Procesando item: " + item.getProductName() + 
                                 " - Cantidad: " + item.getQuantity() + 
                                 " - Precio: $" + item.getPrice());
                
                // Insertar en SaleItems
                insertSaleItem(saleId, item);
                
                // Actualizar stock
                updateProductStock(item.getProductId(), item.getQuantity());
                
                // Registrar movimiento CON FECHA EXPLÍCITA
                registerInventoryMovement(item.getProductId(), "OUT", item.getQuantity(), saleId, saleTimestamp);
            }
            
            connection.commit();
            
            // Mensaje de éxito
            String mensajeExito = String.format(
                "✅ Venta #%d procesada exitosamente\n\n" +
                "Total: $%.2f\n" +
                "Método: %s\n" +
                "Cliente: %s\n" +
                "Fecha: %s\n" +
                "Items: %d",
                saleId, getTotalAmount(), metodoPagoActual, getClienteCompleto(), 
                new SimpleDateFormat("dd/MM/yyyy HH:mm").format(currentDate), cartItems.size()
            );
            
            // Generar factura/boleta si el usuario lo solicitó
            if (generarFactura == JOptionPane.YES_OPTION) {
                boolean facturaGenerada = generateInvoiceForCurrentSale(saleId);
                if (facturaGenerada) {
                    mensajeExito += "\n\n📄 Factura generada exitosamente";
                }
            }
            
            JOptionPane.showMessageDialog(this, mensajeExito, "Venta Exitosa", JOptionPane.INFORMATION_MESSAGE);
            
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

    // ==================== MÉTODO MEJORADO PARA GENERAR FACTURA ====================
    private boolean generateInvoiceForCurrentSale(int saleId) {
        try {
            // Construir contenido de productos para el PDF
            StringBuilder invoiceContent = new StringBuilder();
            double totalVenta = 0;
            
            for (CartItem item : cartItems.values()) {
                invoiceContent.append(String.format("%s|%d|%.2f|%.2f\n", 
                    item.getProductName(), item.getQuantity(), item.getPrice(), item.getSubtotal()));
                totalVenta += item.getSubtotal();
            }

            // Generar PDF con el nuevo diseño profesional
            String fileName = "factura_venta_" + saleId + "_" + 
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".pdf";
            
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new java.io.File(fileName));
            fileChooser.setDialogTitle("Guardar Factura como PDF");

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File selectedFile = fileChooser.getSelectedFile();
                if (!selectedFile.getName().toLowerCase().endsWith(".pdf")) {
                    selectedFile = new java.io.File(selectedFile.getAbsolutePath() + ".pdf");
                }

                boolean success = PDFGenerator.generateInvoicePDF(
                    invoiceContent.toString(), 
                    "FACTURA N°: " + saleId,
                    selectedFile.getAbsolutePath(),
                    getClienteCompleto(),
                    clienteDNI,
                    clienteRUC,
                    currentUser,
                    metodoPagoActual
                );

                if (success) {
                    // Preguntar si desea abrir el PDF
                    int openFile = JOptionPane.showConfirmDialog(this,
                        "¿Desea abrir la factura PDF ahora?",
                        "Abrir Factura",
                        JOptionPane.YES_NO_OPTION);
                    
                    if (openFile == JOptionPane.YES_OPTION) {
                        try {
                            java.awt.Desktop.getDesktop().open(selectedFile);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this,
                                "Factura guardada en: " + selectedFile.getAbsolutePath(),
                                "Información",
                                JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    return true;
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "❌ Error al generar la factura", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            return false;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error generando factura: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // ==================== MÉTODOS DE BASE DE DATOS ====================
    private void loadSales() {
        try {
            // VERSIÓN ACTUALIZADA que usa ClienteNombre
            String sql = "SELECT s.SaleId, COALESCE(s.ClienteNombre, c.Name, 'Cliente Mostrador') as Cliente, " +
                        "u.Username, s.SaleDate, s.TotalAmount, s.PaymentMethod " +
                        "FROM Sales s " +
                        "LEFT JOIN Customers c ON s.CustomerId = c.CustomerId " +
                        "LEFT JOIN Users u ON s.UserId = u.UserId " +
                        "ORDER BY s.SaleDate DESC";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            salesModel.setRowCount(0);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            // Obtener fecha actual para resaltar ventas de hoy
            java.util.Date today = new java.util.Date();
            SimpleDateFormat todayFormat = new SimpleDateFormat("dd/MM/yyyy");
            String todayStr = todayFormat.format(today);
            
            int ventasHoy = 0;
            double totalHoy = 0;
            
            while (rs.next()) {
                String fechaVenta = dateFormat.format(rs.getTimestamp("SaleDate"));
                double total = rs.getDouble("TotalAmount");
                
                // Contar ventas de hoy
                if (fechaVenta.startsWith(todayStr)) {
                    ventasHoy++;
                    totalHoy += total;
                }
                
                salesModel.addRow(new Object[]{
                    rs.getInt("SaleId"),
                    rs.getString("Cliente"),
                    rs.getString("Username"),
                    fechaVenta,
                    String.format("$%.2f", total),
                    rs.getString("PaymentMethod")
                });
            }
            
            rs.close();
            stmt.close();
            
            System.out.println("🛒 Ventas cargadas - Hoy: " + ventasHoy + " - Total Hoy: $" + totalHoy);
            
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
                String.format("$%.2f", item.getPrice()),
                item.getQuantity(),
                String.format("$%.2f", item.getSubtotal())
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
        // Limpiar también datos del cliente
        clienteNombre = "";
        clienteApellidos = "";
        clienteDNI = "";
        clienteRUC = "";
        metodoPagoActual = "Efectivo";
        JOptionPane.showMessageDialog(this, "Carrito limpiado");
    }

    private void updateTotal() {
        double total = 0;
        for (CartItem item : cartItems.values()) {
            total += item.getSubtotal();
        }
        lblTotal.setText(String.format("TOTAL: $%.2f", total));
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

    private void registerInventoryMovement(int productId, String type, int quantity, int saleId, java.sql.Timestamp timestamp) throws SQLException {
        try {
            String sqlMovement = "INSERT INTO InventoryMovements (ProductId, MovementType, Quantity, UserId, Notes, CreatedAt) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmtMovement = connection.prepareStatement(sqlMovement);
            stmtMovement.setInt(1, productId);
            stmtMovement.setString(2, type);
            stmtMovement.setInt(3, quantity);
            stmtMovement.setInt(4, getCurrentUserId());
            stmtMovement.setString(5, "Venta #" + saleId);
            stmtMovement.setTimestamp(6, timestamp); // FECHA EXPLÍCITA
            stmtMovement.executeUpdate();
            stmtMovement.close();
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            System.out.println("✅ Movimiento registrado - ProductId: " + productId + " - Tipo: " + type + 
                             " - Cantidad: " + quantity + " - Fecha: " + dateFormat.format(timestamp));
        } catch (SQLException e) {
            System.err.println("❌ Error registrando movimiento: " + e.getMessage());
            throw e;
        }
    }

    // Sobrecarga del método para mantener compatibilidad
    private void registerInventoryMovement(int productId, String type, int quantity, int saleId) throws SQLException {
        java.sql.Timestamp timestamp = new java.sql.Timestamp(new java.util.Date().getTime());
        registerInventoryMovement(productId, type, quantity, saleId, timestamp);
    }

    private void updateSalesStatistics() {
        try {
            // Usar SIEMPRE la fecha del sistema Java
            java.util.Date today = new java.util.Date();
            SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String todayStr = dbDateFormat.format(today);
            
            System.out.println("📊 Calculando estadísticas para: " + todayStr);
            
            // Ventas de hoy usando fecha del sistema
            String sqlToday = "SELECT COUNT(*) as ventasHoy, COALESCE(SUM(TotalAmount), 0) as totalHoy " +
                             "FROM Sales " +
                             "WHERE CONVERT(DATE, SaleDate) = CONVERT(DATE, ?)";
            
            PreparedStatement stmt = connection.prepareStatement(sqlToday);
            stmt.setString(1, todayStr);
            ResultSet rs = stmt.executeQuery();
            
            int ventasHoy = 0;
            double totalHoy = 0;
            
            if (rs.next()) {
                ventasHoy = rs.getInt("ventasHoy");
                totalHoy = rs.getDouble("totalHoy");
            }
            
            // También obtener estadísticas de productos en carrito
            int totalItemsCarrito = cartItems.values().stream()
                    .mapToInt(CartItem::getQuantity)
                    .sum();
            
            double totalCarrito = getTotalAmount();
            
            lblStats.setText(String.format(
                " Ventas Hoy: %d | Total Hoy: $%.2f |", 
                ventasHoy, totalHoy, totalItemsCarrito, totalCarrito
            ));
            
            rs.close();
            stmt.close();
            
            System.out.println("✅ Estadísticas - Fecha: " + todayStr + " - Ventas: " + ventasHoy + " - Total: $" + totalHoy);
            
        } catch (SQLException e) {
            lblStats.setText("Error cargando estadísticas: " + e.getMessage());
            System.err.println("❌ Error en estadísticas: " + e.getMessage());
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