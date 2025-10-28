package javaapplication36.test;

import javaapplication36.util.DBConnectionManager;
import java.sql.Connection;
import java.sql.SQLException;

public class safeTestDatabase {
    
    public static void main(String[] args) {
        System.out.println("🧪 PRUEBAS SEGURAS - SOLO LECTURA");
        System.out.println("===================================\n");
        
        
        testConexionBaseDatos();  // ← NUEVO TEST
        
        System.out.println("\n✅ PRUEBAS SEGURAS COMPLETADAS");
    }
    
   
    public static void testConexionBaseDatos() {
        System.out.println("📦 TEST 3: Verificar conexión a base de datos");
        
        Connection conn = null;
        try {
            // Intentar obtener conexión
            conn = DBConnectionManager.getConnection();
            
            if (conn != null && !conn.isClosed()) {
                System.out.println("   ✅ Conexión a BD establecida correctamente");
                System.out.println("   ✅ Base de datos: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("   ✅ URL: " + conn.getMetaData().getURL());
            } else {
                System.out.println("   ❌ No se pudo establecer conexión");
            }
            
        } catch (SQLException e) {
            System.out.println("   ❌ Error de conexión: " + e.getMessage());
        } finally {
            // IMPORTANTE: NO cerramos la conexión para no afectar tu app
            System.out.println("   💡 Conexión mantenida abierta para la aplicación");
        }
    }
}