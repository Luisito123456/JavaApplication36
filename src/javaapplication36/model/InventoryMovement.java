/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication36.model;



import java.util.Date;


import java.util.Date;

public class InventoryMovement {

    public InventoryMovement(int i, String pisco_Quebranta, String in, int i0, String lgomez, String string) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public Object getProductName() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public Object getUserName() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    // Enum para tipos de movimiento
    public enum MovementType {
        IN,
        OUT,
        ADJUST
    }

    private int id;
    private Product product;
    private MovementType type;
    private int quantity;
    private String reference;
    private String notes;
    private Date createdAt;
    private User user;

    // Constructor vacío
    public InventoryMovement() {}

    // Constructor completo
    public InventoryMovement(int id, Product product, MovementType type, int quantity,
                             String reference, String notes, Date createdAt, User user) {
        this.id = id;
        this.product = product;
        this.type = type;
        this.quantity = quantity;
        this.reference = reference;
        this.notes = notes;
        this.createdAt = createdAt;
        this.user = user;
    }

    // Getters y Setters
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

    public MovementType getType() {
        return type;
    }

    public void setType(MovementType type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
