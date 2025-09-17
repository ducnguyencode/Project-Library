package UI;

import DAO.LibSettingsDAO;
import Util.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Map;

public class SettingsDialog extends JDialog {
    private JSpinner spDueDays;
    private JSpinner spMaxBooks;
    private JTextField txtLateFee;
    private JTextField txtDepositMul;
    private JButton btnSave;

    public SettingsDialog(Window owner) {
        super(owner, "Library Settings", ModalityType.APPLICATION_MODAL);
        buildUI();
        loadLatest();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(12,12,12,12));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.anchor = GridBagConstraints.WEST;

        int y = 0;
        g.gridx=0; g.gridy=y; p.add(new JLabel("Default Due Days:"), g);
        spDueDays = new JSpinner(new SpinnerNumberModel(5, 1, 60, 1));
        g.gridx=1; g.gridy=y; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; p.add(spDueDays, g);

        y++; g.gridx=0; g.gridy=y; g.fill = GridBagConstraints.NONE; g.weightx=0; p.add(new JLabel("Late Fee / day:"), g);
        txtLateFee = new JTextField("0.10", 10);
        g.gridx=1; g.gridy=y; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; p.add(txtLateFee, g);

        y++; g.gridx=0; g.gridy=y; g.fill = GridBagConstraints.NONE; g.weightx=0; p.add(new JLabel("Max Books / Patron:"), g);
        spMaxBooks = new JSpinner(new SpinnerNumberModel(5, 1, 30, 1));
        g.gridx=1; g.gridy=y; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; p.add(spMaxBooks, g);

        y++; g.gridx=0; g.gridy=y; g.fill = GridBagConstraints.NONE; g.weightx=0; p.add(new JLabel("Deposit Multiplier:"), g);
        txtDepositMul = new JTextField("1.0", 10);
        g.gridx=1; g.gridy=y; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; p.add(txtDepositMul, g);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSave = new JButton("Save New Snapshot");
        JButton btnClose = new JButton("Close");
        btns.add(btnSave); btns.add(btnClose);

        setLayout(new BorderLayout());
        add(p, BorderLayout.CENTER); add(btns, BorderLayout.SOUTH);

        btnClose.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> onSave());

        applyPermissionsByRole();
    }

    private void applyPermissionsByRole() {
        String role = (Session.getRole() == null) ? "" : Session.getRole().toUpperCase();
        boolean canDueDays=false, canMaxBooks=false, canLateFee=false, canDeposit=false;
        switch (role) {
            case "ADMIN":
                canDueDays = canMaxBooks = canLateFee = canDeposit = true; break;
            case "MANAGER":
                canDueDays = canMaxBooks = canLateFee = true; canDeposit = false; break;
            default: // LIBRARIAN or others: view only
                break;
        }
        spDueDays.setEnabled(canDueDays);
        spMaxBooks.setEnabled(canMaxBooks);
        txtLateFee.setEnabled(canLateFee);
        txtDepositMul.setEnabled(canDeposit);
        btnSave.setEnabled(canDueDays || canMaxBooks || canLateFee || canDeposit);
    }

    private void loadLatest() {
        try {
            LibSettingsDAO dao = new LibSettingsDAO();
            Map<String,Object> m = dao.getLatest();
            if (m == null) return;
            spDueDays.setValue(((Number)m.get("DefaultDueDays")).intValue());
            txtLateFee.setText(String.valueOf(m.get("LateFeePerDay")));
            spMaxBooks.setValue(((Number)m.get("MaxBooksPerPatron")).intValue());
            txtDepositMul.setText(String.valueOf(m.get("DepositMultiplier")));
        } catch (Exception ignored) {}
    }

    private void onSave() {
        try {
            int due = ((Number)spDueDays.getValue()).intValue();
            BigDecimal fee = new BigDecimal(txtLateFee.getText().trim());
            int maxB = ((Number)spMaxBooks.getValue()).intValue();
            BigDecimal dep = new BigDecimal(txtDepositMul.getText().trim());

            LibSettingsDAO dao = new LibSettingsDAO();
            Long uid = Session.getUserId();
            long id = dao.insertNew(due, fee, maxB, dep, uid);
            JOptionPane.showMessageDialog(this, "Saved as SettingID="+id);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: "+ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

