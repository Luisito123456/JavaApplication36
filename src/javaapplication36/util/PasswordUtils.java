/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication36.util;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordUtils {

    // Hashea la contraseña usando SHA-256
    public static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al hashear la contraseña", e);
        }
    }

    // Verifica si la contraseña coincide con el hash
    public static boolean check(String password, String storedHash) {
        String hashOfInput = hash(password);
        return hashOfInput.equals(storedHash);
    }

    // Método de prueba
    public static void main(String[] args) {
        String password = "12345";
        String hashed = hash(password);
        System.out.println("Hash: " + hashed);
        System.out.println("Check correcto: " + check("12345", hashed));
        System.out.println("Check incorrecto: " + check("1234", hashed));
    }
}
