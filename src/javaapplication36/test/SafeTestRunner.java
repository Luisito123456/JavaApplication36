package javaapplication36.test;

import javaapplication36.model.Sale;
import javaapplication36.model.SaleItem;
import javaapplication36.service.SalesService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SafeTestRunner {
    
    public static void main(String[] args) {
        System.out.println("PRUEBAS SEGURAS - SOLO LECTURA");
        System.out.println("===================================\n");
        
        
        testFuncionamiento();
        
        System.out.println("\n✅ PRUEBAS SEGURAS COMPLETADAS");
    }
    
    
    
    public static void testFuncionamiento() {
        System.out.println(" TEST 2: Verificar modelos básicos");
        
           try {
        
        Sale venta = new Sale();
        SaleItem item = new SaleItem();
        
        
        BigDecimal precio = new BigDecimal("25.00");
        int cantidad = 2;
        
        item.setUnitPrice(precio);
        item.setQuantity(cantidad);
        
        // ESTABLECER relaciones
        List<SaleItem> items = new ArrayList<>();
        items.add(item);
        venta.setItems(items);
        
        // TESTEO
        
        // Verificar que los datos se guardaron correctamente
        if (item.getUnitPrice().equals(precio)) {
            System.out.println("    Precio unitario configurado correctamente: $" + item.getUnitPrice());
        } else {
            System.out.println("    Error: Precio no coincide");
            return; // Salir si hay error
        }
        
        if (item.getQuantity() == cantidad) {
            System.out.println("    Cantidad configurada correctamente: " + item.getQuantity());
        } else {
            System.out.println("    Error: Cantidad no coincide");
            return;
        }
        
        // Verificar que la venta tiene los items
        if (venta.getItems().size() == 1) {
            System.out.println("    Venta contiene los items correctamente");
        } else {
            System.out.println("    Error: Venta no tiene items");
            return;
        }
        
        System.out.println("   Todas las verificaciones pasaron - Modelos funcionan correctamente");
        
    } catch (Exception e) {
        System.out.println("   Error con modelos: " + e.getMessage());
    }
}
}