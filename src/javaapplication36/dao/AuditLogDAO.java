/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication36.dao;

import javaapplication36.model.AuditEntry;
import javaapplication36.util.DBConnectionManager;




import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    public void create(AuditEntry entry) throws SQLException {
        String sql = "INSERT INTO AuditLog (TableName, RecordId, Action, UserId, Details, CreatedAt) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entry.getTable());
            ps.setString(2, entry.getRecordId());
            ps.setString(3, entry.getAction());
            ps.setInt(4, entry.getUser() != null ? entry.getUser().getId() : null);
            ps.setString(5, entry.getDetails());
            ps.setTimestamp(6, new Timestamp(entry.getCreatedAt().getTime()));
            ps.executeUpdate();
        }
    }

    public List<AuditEntry> findAll() throws SQLException {
        List<AuditEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM AuditLog";
        try (Connection conn = DBConnectionManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                AuditEntry e = new AuditEntry();
                e.setId(rs.getInt("AuditId"));
                e.setTable(rs.getString("TableName"));
                e.setRecordId(rs.getString("RecordId"));
                e.setAction(rs.getString("Action"));
                // Solo ID básico de User
                // e.setUser(new User(rs.getInt("UserId")));
                e.setDetails(rs.getString("Details"));
                e.setCreatedAt(rs.getTimestamp("CreatedAt"));
                list.add(e);
            }
        }
        return list;
    }
}
