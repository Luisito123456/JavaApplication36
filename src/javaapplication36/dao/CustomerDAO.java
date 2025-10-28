/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication36.dao;


import javaapplication36.model.Customer;
import javaapplication36.util.DBConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT CustomerID, FullName, Email, Phone, Address FROM Customers";

        try (Connection conn = DBConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Customer c = new Customer();
                c.setId(rs.getInt("CustomerID"));
                c.setFullName(rs.getString("FullName"));
                c.setEmail(rs.getString("Email"));
                c.setPhone(rs.getString("Phone"));
                c.setAddress(rs.getString("Address"));
                customers.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customers;
    }

    public void create(Customer c) {
        String sql = "INSERT INTO Customers (FullName, Email, Phone, Address) VALUES (?,?,?,?)";

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getFullName());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getAddress());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
