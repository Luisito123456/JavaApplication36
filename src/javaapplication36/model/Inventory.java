/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication36.model;


import java.util.Date;

public class Inventory {
    private int id;
    private int productId;
    private int quantity;
    private Date lastUpdated;

    // Constructor vacío
    public Inventory() {}

    // Constructor con parámetros
    public Inventory(int id, int productId, int quantity, Date lastUpdated) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.lastUpdated = lastUpdated;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // Método auxiliar para aumentar o reducir stock
    public void adjustQuantity(int delta) {
        this.quantity += delta;
        if (this.quantity < 0) this.quantity = 0;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "id=" + id +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
