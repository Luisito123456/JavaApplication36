/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication36.dao;
import javaapplication36.model.InventoryMovement;
import javaapplication36.model.MovementType;
import javaapplication36.model.Product;
import javaapplication36.model.User;
import javaapplication36.util.DBConnectionManager;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;



public class InventoryDAO {

    private Connection conn;

    public InventoryDAO() throws SQLException {
        conn = DBConnectionManager.getConnection();
    }

    // Insertar movimiento de inventario
    public void insertMovement(InventoryMovement mov) throws SQLException {
        String sql = "INSERT INTO InventoryMovements (ProductId, MovementType, Quantity, Reference, Notes, UserId, CreatedAt) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mov.getProduct().getId());
            ps.setString(2, mov.getType() != null ? mov.getType().name() : null);
            ps.setInt(3, mov.getQuantity());
            ps.setString(4, mov.getReference());
            ps.setString(5, mov.getNotes());
            ps.setObject(6, mov.getUser() != null ? mov.getUser().getId() : null, java.sql.Types.INTEGER);
            ps.setTimestamp(7, new Timestamp(mov.getCreatedAt().getTime()));
            ps.executeUpdate();
        }
    }

    // Obtener todos los movimientos
    public List<InventoryMovement> findAll() throws SQLException {
        List<InventoryMovement> list = new ArrayList<>();
        String sql = "SELECT * FROM InventoryMovements";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                InventoryMovement m = new InventoryMovement();
                
                m.setId(rs.getInt("MovementId"));

                // Producto
                Product p = new Product();
                p.setId(rs.getInt("ProductId"));
                m.setProduct(p);

                // Tipo de movimiento seguro
                String typeStr = rs.getString("MovementType");
                if (typeStr != null) {
                    try {
                        
                    } catch (IllegalArgumentException e) {
                        m.setType(null);
                        System.err.println("Tipo de movimiento inválido: " + typeStr);
                    }
                }

                m.setQuantity(rs.getInt("Quantity"));
                m.setReference(rs.getString("Reference"));
                m.setNotes(rs.getString("Notes"));
                m.setCreatedAt(rs.getTimestamp("CreatedAt"));

                // Usuario
                int userId = rs.getInt("UserId");
                if (!rs.wasNull()) {
                    User u = new User();
                    u.setId(userId);
                    m.setUser(u);
                }

                list.add(m);
            }
        }
        return list;
    }

    // Buscar movimientos por producto
    public List<InventoryMovement> findByProduct(int productId) throws SQLException {
        List<InventoryMovement> list = new ArrayList<>();
        String sql = "SELECT * FROM InventoryMovements WHERE ProductId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InventoryMovement m = new InventoryMovement();
                    m.setId(rs.getInt("MovementId"));

                    Product p = new Product();
                    p.setId(rs.getInt("ProductId"));
                    m.setProduct(p);

                    String typeStr = rs.getString("MovementType");
                    if (typeStr != null) {
                        try {
                            
                        } catch (IllegalArgumentException e) {
                            m.setType(null);
                            System.err.println("Tipo de movimiento inválido: " + typeStr);
                        }
                    }

                    m.setQuantity(rs.getInt("Quantity"));
                    m.setReference(rs.getString("Reference"));
                    m.setNotes(rs.getString("Notes"));
                    m.setCreatedAt(rs.getTimestamp("CreatedAt"));

                    int userId = rs.getInt("UserId");
                    if (!rs.wasNull()) {
                        User u = new User();
                        u.setId(userId);
                        m.setUser(u);
                    }

                    list.add(m);
                }
            }
        }
        return list;
    }
}

