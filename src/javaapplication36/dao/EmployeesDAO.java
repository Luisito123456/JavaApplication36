/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication36.dao;

import javaapplication36.model.Employee;
import javaapplication36.util.DBConnectionManager;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeesDAO {

    public List<Employee> findAll() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT EmployeeID, FullName, Role, Phone, HireDate FROM Employees";

        try (Connection conn = DBConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Employee e = new Employee();
                e.setId(rs.getInt("EmployeeID"));
                e.setFullName(rs.getString("FullName"));
                e.setRole(rs.getString("Role"));
                e.setPhone(rs.getString("Phone"));
                e.setHireDate(rs.getDate("HireDate"));
                employees.add(e);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return employees;
    }

    public void create(Employee e) {
        String sql = "INSERT INTO Employees (FullName, Role, Phone, HireDate) VALUES (?,?,?,?)";

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, e.getFullName());
            ps.setString(2, e.getRole());
            ps.setString(3, e.getPhone());
            ps.setDate(4, new java.sql.Date(e.getHireDate().getTime()));
            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

