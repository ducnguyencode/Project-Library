// UI/ChangePasswordDialog.java
package UI;

import DAO.UserDAO;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {
    private final JPasswordField pfOld = new JPasswordField(18);
    private final JPasswordField pfNew = new JPasswordField(18);
    private final JPasswordField pfCf  = new JPasswordField(18);

    private boolean changed = false;

    private ChangePasswordDialog(Window owner, long userId, UserDAO dao, boolean firstLogin) {
        super(owner, "Change Password", ModalityType.APPLICATION_MODAL);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,8,6,8); g.anchor = GridBagConstraints.WEST;

        int y=0;
        if (!firstLogin) {
            g.gridx=0; g.gridy=y; form.add(new JLabel("Current:"), g);
            g.gridx=1;            form.add(pfOld, g);
            y++;
        }
        g.gridx=0; g.gridy=y; form.add(new JLabel("New password:"), g);
        g.gridx=1;            form.add(pfNew, g); y++;
        g.gridx=0; g.gridy=y; form.add(new JLabel("Confirm:"), g);
        g.gridx=1;            form.add(pfCf,  g); y++;

        JButton ok = new JButton("OK"), cancel = new JButton("Cancel");
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(ok); south.add(cancel);

        ok.addActionListener(e -> {
            String oldPw = new String(pfOld.getPassword());
            String nw    = new String(pfNew.getPassword());
            String cf    = new String(pfCf.getPassword());
            if (nw.isBlank() || !nw.equals(cf)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match.");
                return;
            }
            try {
                boolean ok1 = firstLogin
                		? dao.changePassword(userId, null, nw) // nếu lần đầu, old=đã nhập ở form login; nhưng để an toàn cứ gọi bình thường
                        : dao.changePassword(userId, oldPw, nw);
                if (!ok1) {
                    JOptionPane.showMessageDialog(this, "Current password is incorrect.");
                    return;
                }
                changed = true;
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancel.addActionListener(e -> { changed = false; dispose(); });

        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(south, BorderLayout.SOUTH);
        pack(); setLocationRelativeTo(owner);
    }

    public static boolean show(Window owner, long userId, UserDAO dao, boolean firstLogin) {
        ChangePasswordDialog d = new ChangePasswordDialog(owner, userId, dao, firstLogin);
        d.setVisible(true);
        return d.changed;
    }
}
