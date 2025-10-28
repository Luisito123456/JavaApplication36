/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication36.service;


import javaapplication36.model.Sale;
import javaapplication36.model.SaleItem;
import javaapplication36.model.InventoryMovement;
import javaapplication36.dao.SaleDAO;
import javaapplication36.dao.InventoryMovementDAO;
import javaapplication36.util.DBConnectionManager;



import java.math.BigDecimal;
import java.sql.SQLException;

public class SalesService {
    private SaleDAO saleDAO;

    public SalesService() {
        saleDAO = new SaleDAO();
    }

    public void processSale(Sale sale) throws SQLException {
        // Calcula total antes de guardar
        BigDecimal total = calculateTotal(sale);
        sale.setTotalAmount(total);

        // Guarda la venta en la base de datos
        saleDAO.createSale(sale);
    }

    public BigDecimal calculateTotal(Sale sale) {
        return sale.getItems().stream()
                   .map(SaleItem::getLineTotal)
                   .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
