/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication36.util;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionManager {
   private static final String URL = 
    "jdbc:sqlserver://localhost:1433;databaseName=BartenderDB;encrypt=true;trustServerCertificate=true";

    private static final String USER = "sa";
    private static final String PASSWORD = "123456";

    private static Connection connection;

    // Obtener conexión
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                System.err.println("Driver de SQL Server no encontrado.");
                e.printStackTrace();
            }
        }
        return connection;
    }

    // Cerrar conexión
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
