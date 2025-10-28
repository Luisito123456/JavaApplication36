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



public class InventoryMovementDAO {

    private Connection conn;

    public InventoryMovementDAO() throws SQLException {
        conn = DBConnectionManager.getConnection();
    }

    // Insertar un movimiento de inventario
    public void insertMovement(InventoryMovement mov) throws SQLException {
        String sql = "INSERT INTO InventoryMovements (ProductId, MovementType, Quantity, Reference, Notes, CreatedAt, UserId) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mov.getProduct().getId());
            ps.setString(2, mov.getType().name()); // MovementType -> String
            ps.setInt(3, mov.getQuantity());
            ps.setString(4, mov.getReference());
            ps.setString(5, mov.getNotes());
            ps.setTimestamp(6, new Timestamp(mov.getCreatedAt().getTime()));
            ps.setInt(7, mov.getUser() != null ? mov.getUser().getId() : Types.NULL);

            ps.executeUpdate();
        }
    }

    // Obtener todos los movimientos
    public List<InventoryMovement> findAll() throws SQLException {
        List<InventoryMovement> list = new ArrayList<>();
        String sql = "SELECT * FROM InventoryMovements";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                InventoryMovement m = new InventoryMovement();

                Product p = new Product();
                p.setId(rs.getInt("ProductId"));
                m.setProduct(p);

                String typeStr = rs.getString("MovementType");
               try {
} catch (IllegalArgumentException e) {
    m.setType(null); // o manejar error de otra forma
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

        return list;
    }

    // Opcional: obtener movimientos por producto
    public List<InventoryMovement> findByProductId(int productId) throws SQLException {
        List<InventoryMovement> list = new ArrayList<>();
        String sql = "SELECT * FROM InventoryMovements WHERE ProductId = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                InventoryMovement m = new InventoryMovement();

                Product p = new Product();
                p.setId(productId);
                m.setProduct(p);

                String typeStr = rs.getString("MovementType");
              try {
} catch (IllegalArgumentException e) {
    m.setType(null); // o manejar error de otra forma
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

        return list;
    }
}

