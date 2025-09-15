// UI/PatronDialog.java
package UI;

import Model.Patron;

import javax.swing.*;
import java.awt.*;

public final class PatronDialog extends JDialog {
    private final JTextField tfPatronId = new JTextField(16);
    private final JTextField tfName     = new JTextField(22);
    private final JTextField tfAddress  = new JTextField(24);
    private final JTextField tfPhone    = new JTextField(16);
    private final JTextField tfEmail    = new JTextField(22);
    private final JCheckBox  cbActive   = new JCheckBox("Active", true);

    private Patron model; // null => add, !null => edit

    private PatronDialog(Window owner, Patron cur) {
        super(owner, "Patron", ModalityType.APPLICATION_MODAL);
        this.model = cur;

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4,6,4,6);
        g.anchor = GridBagConstraints.WEST;

        int y=0;
        g.gridx=0; g.gridy=y; form.add(new JLabel("Patron ID:"), g);
        g.gridx=1;           form.add(tfPatronId, g);
        y++;
        g.gridx=0; g.gridy=y; form.add(new JLabel("Name:"), g);
        g.gridx=1;            form.add(tfName, g);
        y++;
        g.gridx=0; g.gridy=y; form.add(new JLabel("Address:"), g);
        g.gridx=1;            form.add(tfAddress, g);
        y++;
        g.gridx=0; g.gridy=y; form.add(new JLabel("Phone:"), g);
        g.gridx=1;            form.add(tfPhone, g);
        y++;
        g.gridx=0; g.gridy=y; form.add(new JLabel("Email:"), g);
        g.gridx=1;            form.add(tfEmail, g);
        y++;
        g.gridx=1; g.gridy=y; form.add(cbActive, g);

        if (cur != null) {
            tfPatronId.setText(cur.patronId);
            tfName.setText(cur.name);
            tfAddress.setText(cur.address);
            tfPhone.setText(cur.phone);
            tfEmail.setText(cur.email);
            cbActive.setSelected(cur.isActive);
        }

        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(ok); south.add(cancel);

        ok.addActionListener(e -> {
            if (tfPatronId.getText().isBlank() || tfName.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Patron ID & Name are required"); return;
            }
            Patron p = (model==null ? new Patron() : model);
            p.patronId = tfPatronId.getText().trim();
            p.name     = tfName.getText().trim();
            p.address  = tfAddress.getText().trim();
            p.phone    = tfPhone.getText().trim();
            p.email    = tfEmail.getText().trim();
            p.isActive = cbActive.isSelected();
            model = p;
            dispose();
        });
        cancel.addActionListener(e -> { model=null; dispose(); });

        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(south, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    public static Patron show(Patron cur) {
        Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        PatronDialog d = new PatronDialog(w, cur);
        d.setVisible(true);
        return d.model;
    }
}
