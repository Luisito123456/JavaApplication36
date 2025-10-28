package javaapplication36.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private Connection connection;
    private Map<String, LoginAttempt> loginAttempts;
    private Timer lockTimer;
    private int remainingLockTime = 0;

    // Clase para trackear intentos de login
    private class LoginAttempt {
        int attemptCount = 0;
        long lastAttemptTime = 0;
        boolean isLocked = false;
        long lockUntil = 0;
    }

    public LoginFrame() {
        loginAttempts = new HashMap<>();
        initializeDatabase();
        setupUI();
        startCleanupTimer();
    }

    private void initializeDatabase() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(
                "jdbc:sqlserver://localhost:1433;databaseName=BartenderDB;encrypt=true;trustServerCertificate=true",
                "sa", "123456"
            );
            System.out.println("Conexión a BD establecida correctamente");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error conectando a la base de datos: " + e.getMessage(), 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupUI() {
        setTitle("LICORERIA BARTENDER X - Login");
        setSize(400, 250); // Un poco más alto para mostrar info de bloqueo
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panelFields = new JPanel(new GridLayout(4, 2, 5, 5));
        panelFields.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        panelFields.add(new JLabel("Usuario:"));
        txtUsername = new JTextField();
        panelFields.add(txtUsername);

        panelFields.add(new JLabel("Contraseña:"));
        txtPassword = new JPasswordField();
        panelFields.add(txtPassword);

        // Label para mostrar estado de bloqueo
        panelFields.add(new JLabel("Estado:"));
        JLabel lblStatus = new JLabel("Listo para ingresar");
        lblStatus.setForeground(Color.BLUE);
        lblStatus.setFont(new Font("Arial", Font.BOLD, 10));
        panelFields.add(lblStatus);

        // Información de usuarios de prueba
        JLabel lblInfo = new JLabel("admin/admin123 - vendedor/venta123");
        lblInfo.setForeground(Color.GRAY);
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 9));
        panelFields.add(lblInfo);

        add(panelFields, BorderLayout.CENTER);

        btnLogin = new JButton("Ingresar");
        add(btnLogin, BorderLayout.SOUTH);

        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                authenticateUser();
            }
        });

        // Permitir login con Enter
        txtPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                authenticateUser();
            }
        });

        // Timer para actualizar la interfaz durante bloqueos
        lockTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (remainingLockTime > 0) {
                    remainingLockTime--;
                    lblStatus.setText("Bloqueado: " + remainingLockTime + " segundos");
                    lblStatus.setForeground(Color.RED);
                    
                    if (remainingLockTime <= 0) {
                        lblStatus.setText("Listo para ingresar");
                        lblStatus.setForeground(Color.BLUE);
                        enableLoginFields(true);
                    }
                }
            }
        });
        lockTimer.start();
    }

    private void authenticateUser() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        // Verificar si el usuario está bloqueado
        if (isUserLocked(username)) {
            JOptionPane.showMessageDialog(this, 
                "Usuario temporalmente bloqueado. Intente nuevamente en " + getRemainingLockTime(username) + " segundos.", 
                "Cuenta Bloqueada", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Por favor ingrese usuario y contraseña", 
                "Campos vacíos", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (connection == null) {
            JOptionPane.showMessageDialog(this, 
                "No hay conexión a la base de datos", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String sql = "SELECT UserId, Username, FullName, Role FROM Usuarios WHERE Username = ? AND Password = ? AND IsActive = 1";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Login exitoso - resetear contador de intentos
                resetLoginAttempts(username);
                
                String fullName = rs.getString("FullName");
                String role = rs.getString("Role");
                
                // Actualizar último login
                updateLastLogin(rs.getInt("UserId"));
                
                JOptionPane.showMessageDialog(this, 
                    "Bienvenido: " + fullName + " (" + role + ")", 
                    "Login exitoso", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Abrir dashboard pasando la información del usuario
                DashboardFrame dashboard = new DashboardFrame(username, fullName, role, connection);
                dashboard.setVisible(true);
                dispose(); // cerrar login
            } else {
                // Login fallido - incrementar contador
                handleFailedLogin(username);
                
                JOptionPane.showMessageDialog(this, 
                    "Usuario o contraseña incorrectos\nIntentos fallidos: " + getAttemptCount(username), 
                    "Error de autenticación", 
                    JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
                txtUsername.requestFocus();
            }

            rs.close();
            stmt.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error en la autenticación: " + ex.getMessage(), 
                "Error de BD", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleFailedLogin(String username) {
        LoginAttempt attempt = loginAttempts.getOrDefault(username, new LoginAttempt());
        attempt.attemptCount++;
        attempt.lastAttemptTime = System.currentTimeMillis();
        
        // Bloquear después de 3 intentos fallidos
        if (attempt.attemptCount >= 3) {
            attempt.isLocked = true;
            attempt.lockUntil = System.currentTimeMillis() + (5 * 60 * 1000); // 5 minutos
            remainingLockTime = 5 * 60; // 300 segundos
            enableLoginFields(false);
        }
        
        loginAttempts.put(username, attempt);
    }

    private void resetLoginAttempts(String username) {
        loginAttempts.remove(username);
    }

    private boolean isUserLocked(String username) {
        LoginAttempt attempt = loginAttempts.get(username);
        if (attempt != null && attempt.isLocked) {
            if (System.currentTimeMillis() > attempt.lockUntil) {
                // Tiempo de bloqueo expirado
                resetLoginAttempts(username);
                return false;
            }
            return true;
        }
        return false;
    }

    private int getRemainingLockTime(String username) {
        LoginAttempt attempt = loginAttempts.get(username);
        if (attempt != null && attempt.isLocked) {
            long remaining = (attempt.lockUntil - System.currentTimeMillis()) / 1000;
            return Math.max(0, (int) remaining);
        }
        return 0;
    }

    private int getAttemptCount(String username) {
        LoginAttempt attempt = loginAttempts.get(username);
        return attempt != null ? attempt.attemptCount : 0;
    }

    private void enableLoginFields(boolean enabled) {
        txtUsername.setEnabled(enabled);
        txtPassword.setEnabled(enabled);
        btnLogin.setEnabled(enabled);
    }

    private void startCleanupTimer() {
        // Timer para limpiar intentos antiguos cada hora
        Timer cleanupTimer = new Timer(60 * 60 * 1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cleanupOldAttempts();
            }
        });
        cleanupTimer.start();
    }

    private void cleanupOldAttempts() {
        long currentTime = System.currentTimeMillis();
        loginAttempts.entrySet().removeIf(entry -> {
            LoginAttempt attempt = entry.getValue();
            // Remover intentos con más de 1 hora sin actividad
            return (currentTime - attempt.lastAttemptTime) > (60 * 60 * 1000);
        });
    }

    private void updateLastLogin(int userId) {
        try {
            String sql = "UPDATE Usuarios SET LastLogin = GETDATE() WHERE UserId = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error actualizando último login: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}