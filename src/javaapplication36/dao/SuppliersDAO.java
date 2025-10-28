/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication36.dao;

import javaapplication36.model.Supplier;
import javaapplication36.util.DBConnectionManager;



import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SuppliersDAO {

    public List<Supplier> findAll() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT SupplierID, SupplierName, Contact, Phone, Email FROM Suppliers";

        try (Connection conn = DBConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Supplier s = new Supplier();
                s.setId(rs.getInt("SupplierID"));
                s.setName(rs.getString("SupplierName"));
                s.setContact(rs.getString("Contact"));
                s.setPhone(rs.getString("Phone"));
                s.setEmail(rs.getString("Email"));
                suppliers.add(s);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return suppliers;
    }

    public void create(Supplier s) {
        String sql = "INSERT INTO Suppliers (SupplierName, Contact, Phone, Email) VALUES (?,?,?,?)";

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, s.getName());
            ps.setString(2, s.getContact());
            ps.setString(3, s.getPhone());
            ps.setString(4, s.getEmail());
            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
