/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication36.dao;

import javaapplication36.model.Sale;
import javaapplication36.model.SaleItem;
import javaapplication36.util.DBConnectionManager;


import java.sql.*;
import java.math.BigDecimal;

public class SaleDAO {

    public void createSale(Sale sale) throws SQLException {
        String sqlSale = "INSERT INTO Sales (SaleDate, EmployeeID, TotalAmount, CustomerID) VALUES (?, ?, ?, ?)";
        String sqlItem = "INSERT INTO SalesDetail (SaleID, ProductID, Quantity, UnitPrice) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement psSale = null;
        PreparedStatement psItem = null;

        try {
            conn = DBConnectionManager.getConnection();
            conn.setAutoCommit(false);

            psSale = conn.prepareStatement(sqlSale, Statement.RETURN_GENERATED_KEYS);
            psSale.setTimestamp(1, new Timestamp(sale.getSaleDate().getTime()));
            psSale.setInt(2, sale.getUser().getId());
            psSale.setBigDecimal(3, sale.getTotalAmount());
            if (sale.getCustomer() != null) {
                psSale.setInt(4, sale.getCustomer().getId());
            } else {
                psSale.setNull(4, Types.INTEGER);
            }

            psSale.executeUpdate();
            ResultSet rs = psSale.getGeneratedKeys();
            if (rs.next()) {
                sale.setId(rs.getInt(1));
            }

            psItem = conn.prepareStatement(sqlItem);
            for (SaleItem item : sale.getItems()) {
                psItem.setInt(1, sale.getId());
                psItem.setInt(2, item.getProduct().getId());
                psItem.setInt(3, item.getQuantity());
                psItem.setBigDecimal(4, item.getUnitPrice());
                psItem.addBatch();
            }
            psItem.executeBatch();

            conn.commit();
        } catch (SQLException ex) {
            if (conn != null) conn.rollback();
            throw ex;
        } finally {
            if (psItem != null) psItem.close();
            if (psSale != null) psSale.close();
            if (conn != null) conn.close();
        }
    }
}
