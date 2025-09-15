// UI/LoginFrame.java
package UI;

import DAO.UserDAO;
import Model.SystemUser;

import javax.swing.*;
import java.awt.*;
import Util.Session;

public class LoginFrame extends JFrame {
    private final JTextField tfUser = new JTextField(16);
    private final JPasswordField pfPass = new JPasswordField(16);
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        super("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,8,6,8); g.anchor = GridBagConstraints.WEST;

        g.gridx=0; g.gridy=0; p.add(new JLabel("Username:"), g);
        g.gridx=1;            p.add(tfUser, g);
        g.gridx=0; g.gridy=1; p.add(new JLabel("Password:"), g);
        g.gridx=1;            p.add(pfPass, g);

        JButton btLogin = new JButton("Login");
        JButton btExit  = new JButton("Exit");
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btLogin); south.add(btExit);

        btExit.addActionListener(e -> System.exit(0));
        btLogin.addActionListener(e -> doLogin());

        getContentPane().add(p, BorderLayout.CENTER);
        getContentPane().add(south, BorderLayout.SOUTH);
        pack(); setLocationRelativeTo(null);
    }

    private void doLogin() {
        String u = tfUser.getText().trim();
        String p = new String(pfPass.getPassword());
        if (u.isEmpty() || p.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter username & password."); return; }
        try {
            SystemUser user = userDAO.login(u, p);
            if (user == null) { JOptionPane.showMessageDialog(this, "Invalid credentials or inactive account."); return; }

            boolean firstLogin = (user.lastLogin == null);
            if (firstLogin) {
                JOptionPane.showMessageDialog(this,
                    "First login detected. You must change password before continuing.");
                boolean changed = ChangePasswordDialog.show(this, user.userId, userDAO, true);
                if (!changed) return;
            }

            userDAO.updateLastLogin(user.userId);

            // NEW: gán phiên sau khi mọi thứ OK
            Session.currentUserId   = user.userId;
            Session.currentUsername = user.username;
            Session.currentRole     = user.role;

            EventQueue.invokeLater(() -> {
                new MainFrame().setVisible(true);
                dispose();
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
