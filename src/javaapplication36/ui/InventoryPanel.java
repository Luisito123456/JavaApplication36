package javaapplication36.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class InventoryPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private Connection connection;
    private JLabel lblStats;

    public InventoryPanel(Connection connection) {
        this.connection = connection;
        initializeUI();
        loadMovements();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de estadísticas
        lblStats = new JLabel("Cargando estadísticas...");
        lblStats.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(lblStats, BorderLayout.NORTH);

        // Tabla de movimientos
       model = new DefaultTableModel(new Object[]{
    "ID Movimiento", "Producto", "Tipo", "Cantidad", "Notas", "Usuario", "Fecha"
}, 0);
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnRefresh = new JButton("🔄 Actualizar");
        JButton btnAddMovement = new JButton("➕ Nuevo Movimiento");
        
        btnRefresh.addActionListener(e -> loadMovements());
        btnAddMovement.addActionListener(e -> showAddMovementDialog());
        
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnAddMovement);
        add(buttonPanel, BorderLayout.SOUTH);
    }

   private void loadMovements() {
    try {
        String sql = "SELECT im.MovementId, p.Name, im.MovementType, " +
                    "im.Quantity, im.Notes, " +
                    "u.Username, im.CreatedAt " +
                    "FROM InventoryMovements im " +
                    "INNER JOIN Products p ON im.ProductId = p.ProductId " +
                    "INNER JOIN Users u ON im.UserId = u.UserId " +
                    "ORDER BY im.CreatedAt DESC";
        
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        model.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("MovementId"),
                rs.getString("Name"),
                rs.getString("MovementType"),
                rs.getInt("Quantity"),
                rs.getString("Notes"),
                rs.getString("Username"),
                dateFormat.format(rs.getTimestamp("CreatedAt"))
            });
        }
        
        rs.close();
        stmt.close();
        
        updateStatistics();
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, 
            "Error cargando movimientos: " + e.getMessage(), 
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}
    private void updateStatistics() {
        try {
            // Total movimientos
            String sqlTotal = "SELECT COUNT(*) as total FROM InventoryMovements";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sqlTotal);
            rs.next();
            int totalMovements = rs.getInt("total");
            
            // Movimientos hoy
            String sqlToday = "SELECT COUNT(*) as hoy FROM InventoryMovements WHERE CAST(MovementDate AS DATE) = CAST(GETDATE() AS DATE)";
            rs = stmt.executeQuery(sqlToday);
            rs.next();
            int todayMovements = rs.getInt("hoy");
            
            lblStats.setText(String.format(
                "📊 Estadísticas: %d movimientos totales | %d movimientos hoy", 
                totalMovements, todayMovements
            ));
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            lblStats.setText("Error cargando estadísticas");
        }
    }

    private void showAddMovementDialog() {
        // Aquí implementarías un diálogo para agregar movimientos
        JOptionPane.showMessageDialog(this, 
            "Funcionalidad para agregar movimientos - Próximamente",
            "En desarrollo", 
            JOptionPane.INFORMATION_MESSAGE);
    }
}