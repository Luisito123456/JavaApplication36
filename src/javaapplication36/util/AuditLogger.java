/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication36.util;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class AuditLogger {

    public static void log(String tableName, String recordId, String action, String details, Integer userId) {
        String sql = "INSERT INTO AuditLog (TableName, RecordId, Action, Details, UserId, CreatedAt) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tableName);
            ps.setString(2, recordId);
            ps.setString(3, action);
            ps.setString(4, details);
            if (userId != null) {
                ps.setInt(5, userId);
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            ps.setObject(6, LocalDateTime.now());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error registrando auditoría: " + e.getMessage());
        }
    }

    // Ejemplo de uso
    public static void main(String[] args) {
        log("Products", "1", "INSERT", "Producto agregado: Pisco Quebranta", 1);
    }
}
