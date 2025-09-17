package UI;

import DAO.LoanTicketDAO;
import Util.DB;
import Util.Session;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.List;

public class CheckInForm extends JPanel {
    private JTextArea txtCalls;
    private JButton btnCheckIn;
    private JTextArea txtResult;
    private JButton btnPick;
    private JButton btnPaste;

    public CheckInForm() {
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.anchor = GridBagConstraints.WEST;

        g.gridx=0; g.gridy=0; form.add(new JLabel("Book CallNumber(s) (one per line):"), g);
        txtCalls = new JTextArea(6, 60);
        JScrollPane sp = new JScrollPane(txtCalls);
        g.gridx=1; g.gridy=0; g.fill=GridBagConstraints.BOTH; g.weightx=1; g.weighty=1; form.add(sp, g);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnCheckIn = new JButton("Check In");
        btnPick    = new JButton("Pick...");
        btnPaste   = new JButton("Paste");
        btns.add(btnCheckIn); btns.add(btnPick); btns.add(btnPaste);
        g.gridx=1; g.gridy=1; g.fill=GridBagConstraints.NONE; g.weightx=0; g.weighty=0; form.add(btns, g);

        txtResult = new JTextArea(10, 80);
        txtResult.setEditable(false);

        add(form, BorderLayout.NORTH);
        add(new JScrollPane(txtResult), BorderLayout.CENTER);

        // events
        btnCheckIn.addActionListener(e -> doCheckIn());
        btnPick.addActionListener(e -> onPick());
        btnPaste.addActionListener(e -> onPaste());
    }

    private void doCheckIn() {
        String raw = txtCalls.getText().trim();
        if (raw.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter one or more CallNumbers (one per line)"); return; }
        List<String> calls = java.util.Arrays.stream(raw.split("\\R"))
                .map(String::trim).filter(s->!s.isEmpty()).toList();

        try (Connection c = DB.get()) {
            LoanTicketDAO dao = new LoanTicketDAO(c);
            long userId = Session.currentUserId;
            LoanTicketDAO.CheckinResult res = dao.checkinMany(calls, userId);

            StringBuilder sb = new StringBuilder();
            sb.append("CHECK-IN RESULT\n");
            sb.append("Checked : ").append(res.itemCount).append("\n");
            sb.append("Total Late Fees: ").append(res.totalLateFees).append("\n");
            if (!res.checked.isEmpty()) sb.append("Checked Items: ").append(String.join(", ", res.checked)).append("\n");
            if (!res.skipped.isEmpty()) sb.append("Skipped: ").append(String.join(", ", res.skipped)).append("\n");

            txtResult.setText(sb.toString());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void onPick() {
        Window w = SwingUtilities.getWindowAncestor(this);
        List<String> calls = OpenLoansDialog.pick(w);
        if (calls==null || calls.isEmpty()) return;
        String s = String.join("\n", calls);
        if (!txtCalls.getText().isBlank()) s = "\n"+s;
        txtCalls.append(s);
        txtCalls.requestFocus();
    }

    private void onPaste() {
        try {
            var cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            var data = cb.getData(java.awt.datatransfer.DataFlavor.stringFlavor);
            if (data != null) {
                String s = data.toString().replace("\r\n","\n").replace('\r','\n');
                if (!txtCalls.getText().isBlank()) s = "\n" + s;
                txtCalls.append(s);
                txtCalls.requestFocus();
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }
}
