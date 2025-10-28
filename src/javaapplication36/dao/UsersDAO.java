/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication36.dao;

import javaapplication36.model.User;
import javaapplication36.util.DBConnectionManager;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsersDAO {

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT UserID, Username, PasswordHash, Role, EmployeeID FROM Users";

        try (Connection conn = DBConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("UserID"));
                u.setUsername(rs.getString("Username"));
                u.setPasswordHash(rs.getString("PasswordHash")); 
                u.setRole(rs.getString("Role"));
                u.setEmployeeId(rs.getInt("EmployeeID"));
                users.add(u);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return users;
    }

    public void create(User u) {
        String sql = "INSERT INTO Users (Username, PasswordHash, Role, EmployeeID) VALUES (?,?,?,?)";

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPasswordHash()); // HASH SEGURIDAD CONTRASEÑA
            ps.setString(3, u.getRole());
            ps.setInt(4, u.getEmployeeId());
            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
