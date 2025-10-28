import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestConexionSQL {
     public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=MiBaseDeDatos;encrypt=true;trustServerCertificate=true";
        String user = "sa";
        String password = "12345";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Conexión exitosa a SQL Server!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
