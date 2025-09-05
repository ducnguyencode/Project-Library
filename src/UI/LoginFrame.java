package UI;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginFrame() {
        setTitle("Library Manager - Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        panel.add(new JLabel("Username:"));
        txtUsername = new JTextField();
        panel.add(txtUsername);

        panel.add(new JLabel("Password:"));
        txtPassword = new JPasswordField();
        panel.add(txtPassword);

        btnLogin = new JButton("Login");
        btnLogin.addActionListener(e -> doLogin());
        panel.add(new JLabel());
        panel.add(btnLogin);

        add(panel);
    }

    private void doLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        try {
            Long uid = new DAO.UserDAO().login(user, pass);
            if (uid != null) {
                dispose();
                javax.swing.SwingUtilities.invokeLater(() -> {
                    try { new MainFrame().setVisible(true); }
                    catch (Throwable t) {
                        t.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Lỗi mở màn hình chính: " + t.getMessage());
                    }
                });
            } else {
                JOptionPane.showMessageDialog(this, "Invalid login!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Login error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
