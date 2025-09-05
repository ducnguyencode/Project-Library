import UI.LoginFrame;
import DAO.UserDAO;

import java.awt.EventQueue;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class App {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Throwable e) {
            e.printStackTrace(); 
        }

        try {
            new UserDAO().ensureAdmin("admin", "123", null);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                null,
                "Bootstrap admin error: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
        
        EventQueue.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
