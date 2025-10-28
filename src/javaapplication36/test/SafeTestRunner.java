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
        
        testServicioExiste();
        testModelosFuncionan();
        
        System.out.println("\n✅ PRUEBAS SEGURAS COMPLETADAS");
    }
    
    public static void testServicioExiste() {
        System.out.println(" TEST 1: Verificar que SalesService funciona");
        
        try {
            SalesService service = new SalesService();
            System.out.println("   ✅ SalesService se crea correctamente");
            
            // Verificación para corroborar el test 
            System.out.println("   ✅ Servicio inicializado sin errores");
            
        } catch (Exception e) {
            System.out.println("   ❌ Error creando SalesService: " + e.getMessage());
        }
    }
    
    public static void testModelosFuncionan() {
        System.out.println(" TEST 2: Verificar modelos básicos");
        
        try {
            // Probamos crear objetos sin guardar en BD
            Sale venta = new Sale();
            SaleItem item = new SaleItem();
            
            item.setUnitPrice(new BigDecimal("25.00"));
            item.setQuantity(2);
            
            List<SaleItem> items = new ArrayList<>();
            items.add(item);
            venta.setItems(items);
            
            System.out.println("   ✅ Modelos Sale y SaleItem funcionan");
            System.out.println("   ✅ Precio unitario: $" + item.getUnitPrice());
            System.out.println("   ✅ Cantidad: " + item.getQuantity());
            
        } catch (Exception e) {
            System.out.println("   ❌ Error con modelos: " + e.getMessage());
        }
    }
}