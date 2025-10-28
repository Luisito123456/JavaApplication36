package javaapplication36.ui;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class DashboardFrame extends JFrame {
    private String username;
    private String fullName;
    private String role;
    private Connection connection;
    private JTabbedPane tabs;

    public DashboardFrame(String username, String fullName, String role, Connection connection) {
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.connection = connection;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("LICORERIA BARTENDER X - Sistema de Gestión Integrado");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        createTopPanel();
        createTabs();
        createStatusBar();
        
        // Cargar datos iniciales
        loadInitialData();
    }

    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(44, 62, 80));
        topPanel.setPreferredSize(new Dimension(1200, 70));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Información del usuario
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userPanel.setBackground(new Color(44, 62, 80));
        
        JLabel userIcon = new JLabel("👤");
        userIcon.setFont(new Font("Arial", Font.PLAIN, 20));
        JLabel userInfo = new JLabel(fullName + " (" + role + ")");
        userInfo.setForeground(Color.WHITE);
        userInfo.setFont(new Font("Arial", Font.BOLD, 14));
        
        userPanel.add(userIcon);
        userPanel.add(userInfo);
        topPanel.add(userPanel, BorderLayout.WEST);

        // Título del sistema
        JLabel title = new JLabel("🍸 LICORERIA BARTENDER X - SISTEMA INTEGRADO POS", JLabel.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(title, BorderLayout.CENTER);

        // Botón de cerrar sesión
        JButton btnLogout = new JButton("🚪 Cerrar Sesión");
        btnLogout.setBackground(new Color(231, 76, 60));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font("Arial", Font.BOLD, 12));
        btnLogout.addActionListener(e -> logout());
        topPanel.add(btnLogout, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
    }

    private void createTabs() {
        tabs = new JTabbedPane();
        
        // Pestañas con nombres corregidos - SOLO CAMBIOS DE NOMBRE
        tabs.addTab("📦 Inventario", new ProductPanel(connection)); // Antes: "🍷 Productos"
        tabs.addTab("💰 Punto de Venta", new SalesPanel(connection, username));
        tabs.addTab("🏢 Proveedores", new CustomersPanel(connection)); // Antes: "👥 Clientes" 
        tabs.addTab("🔄 Movimientos", new MovementsPanel(connection));
        tabs.addTab("📊 Reportes", new ReportsPanel(connection));
        
        // Control de acceso por rol
        if ("Seller".equals(role)) {
            tabs.setEnabledAt(2, false); // Proveedores
            tabs.setEnabledAt(3, false); // Movimientos
            tabs.setEnabledAt(4, false); // Reportes
        }

        add(tabs, BorderLayout.CENTER);
    }

    private void createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.setPreferredSize(new Dimension(1200, 30));
        statusPanel.setBackground(new Color(240, 240, 240));
        
        JLabel dbInfo = new JLabel(" Base de datos: BartenderDB | Tablas: Products, Sales, Customers, Categories, Suppliers, InventoryMovements");
        dbInfo.setFont(new Font("Arial", Font.PLAIN, 11));
        
        JLabel userInfo = new JLabel("Usuario: " + username + " | Rol: " + role + " | " + new java.util.Date());
        userInfo.setFont(new Font("Arial", Font.PLAIN, 11));
        
        statusPanel.add(dbInfo, BorderLayout.WEST);
        statusPanel.add(userInfo, BorderLayout.EAST);
        
        add(statusPanel, BorderLayout.SOUTH);
    }

    private void loadInitialData() {
        // Cargar métricas iniciales del sistema
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                checkSystemMetrics();
                return null;
            }
        };
        worker.execute();
    }

    private void checkSystemMetrics() {
        try {
            // Verificar conteo de productos
            String sqlProducts = "SELECT COUNT(*) as total FROM Products WHERE IsActive = 1";
            var stmt = connection.createStatement();
            var rs = stmt.executeQuery(sqlProducts);
            if (rs.next()) {
                int totalProducts = rs.getInt("total");
                System.out.println("✅ Productos en sistema: " + totalProducts);
            }
            
            // Verificar productos con stock bajo
            String sqlLowStock = "SELECT COUNT(*) as lowStock FROM Products WHERE Stock <= ReorderLevel AND IsActive = 1";
            rs = stmt.executeQuery(sqlLowStock);
            if (rs.next()) {
                int lowStock = rs.getInt("lowStock");
                System.out.println("⚠️  Productos con stock bajo: " + lowStock);
            }
            
            // Verificar clientes registrados
            String sqlCustomers = "SELECT COUNT(*) as total FROM Customers";
            rs = stmt.executeQuery(sqlCustomers);
            if (rs.next()) {
                int totalCustomers = rs.getInt("total");
                System.out.println("🏢 Proveedores registrados: " + totalCustomers); // Nombre actualizado
            }
            
            // Verificación de movimientos
            String sqlMovements = "SELECT COUNT(*) as total FROM InventoryMovements WHERE CreatedAt >= DATEADD(day, -7, GETDATE())";
            rs = stmt.executeQuery(sqlMovements);
            if (rs.next()) {
                int recentMovements = rs.getInt("total");
                System.out.println("🔄 Movimientos últimos 7 días: " + recentMovements); // Icono actualizado
            }
            
            rs.close();
            stmt.close();
            
        } catch (Exception e) {
            System.err.println("Error cargando métricas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro que desea cerrar sesión?", 
            "Cerrar Sesión", 
            JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    System.out.println("Conexión a BD cerrada");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LoginFrame().setVisible(true);
            dispose();
        }
    }

    // Getters para los paneles
    public Connection getConnection() {
        return connection;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}