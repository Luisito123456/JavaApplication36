/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication36.service;

import javaapplication36.model.Product;
import javaapplication36.model.User;
import javaapplication36.model.InventoryMovement;
import javaapplication36.model.MovementType;
import javaapplication36.dao.ProductDAO;
import javaapplication36.dao.InventoryDAO;
import javaapplication36.dao.InventoryMovementDAO;
import javaapplication36.util.DBConnectionManager;





import java.util.List;


public class InventoryService {

    private ProductDAO productDAO;
    private InventoryMovementDAO inventoryDAO;

    public InventoryService(ProductDAO productDAO, InventoryMovementDAO inventoryDAO) {
        this.productDAO = productDAO;
        this.inventoryDAO = inventoryDAO;
    }

    // Agregar stock a un producto
    public void addStock(Product p, int qty, User user) throws Exception {
        if (p == null) throw new Exception("Producto no puede ser nulo");
        if (qty <= 0) throw new Exception("Cantidad inválida");

        // Actualizar stock en Product
        p.setStock(p.getStock() + qty);
        productDAO.update(p);

        // Registrar movimiento de inventario
        InventoryMovement mov = new InventoryMovement();
        mov.setProduct(p);
mov.setType(InventoryMovement.MovementType.IN);
        mov.setQuantity(qty);
        mov.setUser(user);
        mov.setReference("Stock agregado manualmente");
        mov.setNotes("Incremento por recepción de inventario");
        inventoryDAO.insertMovement(mov);
    }

    // Reducir stock según venta
    public void reduceStockBySale(Product p, int qty, User user) throws Exception {
        if (p == null) throw new Exception("Producto no puede ser nulo");
        if (qty <= 0 || qty > p.getStock()) throw new Exception("Cantidad inválida");

        // Actualizar stock en Product
        p.setStock(p.getStock() - qty);
        productDAO.update(p);

        // Registrar movimiento de inventario
        InventoryMovement mov = new InventoryMovement();
        mov.setProduct(p);
mov.setType(InventoryMovement.MovementType.OUT);
        mov.setQuantity(qty);
        mov.setUser(user);
        mov.setReference("Venta");
        mov.setNotes("Reducción por venta");
        inventoryDAO.insertMovement(mov);
    }

    // Productos con bajo stock
    public List<Product> getLowStock() throws Exception {
        return productDAO.findLowStock();
    }
}
