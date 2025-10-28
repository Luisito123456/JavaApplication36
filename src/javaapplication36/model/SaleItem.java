/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication36.model;




import java.math.BigDecimal;

public class SaleItem {
    private int id;
    private Product product;
    private int quantity;
    private BigDecimal unitPrice;

    public SaleItem() {
    }

    public SaleItem(Product product, int quantity, BigDecimal unitPrice) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    // Método para calcular el total de la línea
    public BigDecimal getLineTotal() {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(new BigDecimal(quantity));
    }
}
