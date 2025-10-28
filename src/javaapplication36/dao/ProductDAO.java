/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication36.dao;

import java.sql.Connection;




import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import javaapplication36.model.Product;
import javaapplication36.util.DBConnectionManager;

public class ProductDAO {

    // Trae todos los productos con categoría y proveedor
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.ProductId, p.SKU, p.Name, c.Name AS Category, s.Name AS Supplier, " +
                     "p.SalePrice, p.Stock " +
                     "FROM Products p " +
                     "LEFT JOIN Categories c ON p.CategoryId = c.CategoryId " +
                     "LEFT JOIN Suppliers s ON p.SupplierId = s.SupplierId";

        try (Connection conn = DBConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("ProductId"));
                p.setName(rs.getString("Name"));
                p.setSku(rs.getString("SKU"));
                p.setCategory(rs.getString("Category"));
                p.setSupplier(rs.getString("Supplier"));
                p.setSalePrice(rs.getBigDecimal("SalePrice"));
                p.setStock(rs.getInt("Stock"));
                products.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    // Buscar un producto por ID
    public Product findById(int id) {
        Product p = null;
        String sql = "SELECT p.ProductId, p.SKU, p.Name, c.Name AS Category, s.Name AS Supplier, " +
                     "p.SalePrice, p.Stock " +
                     "FROM Products p " +
                     "LEFT JOIN Categories c ON p.CategoryId = c.CategoryId " +
                     "LEFT JOIN Suppliers s ON p.SupplierId = s.SupplierId " +
                     "WHERE p.ProductId = ?";

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p = new Product();
                    p.setId(rs.getInt("ProductId"));
                    p.setName(rs.getString("Name"));
                    p.setSku(rs.getString("SKU"));
                    p.setCategory(rs.getString("Category"));
                    p.setSupplier(rs.getString("Supplier"));
                    p.setSalePrice(rs.getBigDecimal("SalePrice"));
                    p.setStock(rs.getInt("Stock"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return p;
    }

    // Crear un producto
    public void create(Product p) {
        String sql = "INSERT INTO Products (Name, SKU, SalePrice, Stock, CategoryId, SupplierId) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getName());
            ps.setString(2, p.getSku());
            ps.setBigDecimal(3, p.getSalePrice());
            ps.setInt(4, p.getStock());
            // Opcion busqueda NULL
            ps.setObject(5, null); // CategoryId
            ps.setObject(6, null); // SupplierId

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Actualizar un producto
    public void update(Product p) {
        String sql = "UPDATE Products SET Name=?, SKU=?, SalePrice=?, Stock=? WHERE ProductId=?";

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getName());
            ps.setString(2, p.getSku());
            ps.setBigDecimal(3, p.getSalePrice());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Eliminar un producto
    public void delete(int id) {
        String sql = "DELETE FROM Products WHERE ProductId=?";

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Productos con bajo stock
    public List<Product> findLowStock() {
        List<Product> lowStock = new ArrayList<>();
        String sql = "SELECT * FROM Products WHERE Stock <= ReorderLevel";

        try (Connection conn = DBConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("ProductId"));
                p.setName(rs.getString("Name"));
                p.setSku(rs.getString("SKU"));
                p.setSalePrice(rs.getBigDecimal("SalePrice"));
                p.setStock(rs.getInt("Stock"));
                lowStock.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lowStock;
    }
}
