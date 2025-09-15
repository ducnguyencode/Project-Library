package UI;

import DAO.LoanTicketDAO;
import Util.DB;
import Util.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CheckOutForm extends JPanel {

    private JTextField txtEmployee;     // read-only: current user
    private JTextField txtPatronId;     // input: Patron ID (student)
    private JTextArea  txtCalls;        // one call number per line
    private JTextArea  txtResult;       // output area
    private JButton    btnCheckOut;
    private JButton    btnClear;

    public CheckOutForm() {
        buildUI();
        bindEvents();
        loadSessionUser();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new GridBagLayout());
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        GridBagConstraints g = new GridBagConstraints();
        Insets insets = new Insets(6, 6, 6, 6);
        g.insets = insets;
        g.anchor = GridBagConstraints.WEST;

        // Employee (read-only)
        g.gridx = 0; g.gridy = 0;
        top.add(new JLabel("Employee:"), g);
        txtEmployee = new JTextField(18);
        txtEmployee.setEditable(false);
        g.gridx = 1; g.gridy = 0; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        top.add(txtEmployee, g);

        // Patron ID (input)
        g.gridx = 0; g.gridy = 1; g.fill = GridBagConstraints.NONE; g.weightx = 0;
        top.add(new JLabel("Patron ID:"), g);
        txtPatronId = new JTextField(18);
        g.gridx = 1; g.gridy = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        top.add(txtPatronId, g);

        // Call numbers
        g.gridx = 0; g.gridy = 2;
        top.add(new JLabel("CallNumbers (one per line):"), g);
        txtCalls = new JTextArea(8, 60);
        txtCalls.setLineWrap(false);
        JScrollPane spCalls = new JScrollPane(txtCalls);
        g.gridx = 1; g.gridy = 2; g.gridwidth = 1; g.fill = GridBagConstraints.BOTH; g.weightx = 1; g.weighty = 1;
        top.add(spCalls, g);

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnCheckOut = new JButton("Check Out");
        btnClear    = new JButton("Clear");
        btns.add(btnCheckOut);
        btns.add(btnClear);
        g.gridx = 1; g.gridy = 3; g.weightx = 0; g.weighty = 0; g.fill = GridBagConstraints.NONE;
        top.add(btns, g);

        add(top, BorderLayout.NORTH);

        // Result area
        txtResult = new JTextArea(14, 120);
        txtResult.setEditable(false);
        txtResult.setLineWrap(false);
        add(new JScrollPane(txtResult), BorderLayout.CENTER);
    }

    private void bindEvents() {
        // Enter in Patron text triggers checkout if there are calls
        txtPatronId.addActionListener(this::doCheckout);

        // Press Enter while focus in calls area: run checkout (if Ctrl not down)
        txtCalls.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (!e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doCheckout(null);
                }
            }
        });

        btnCheckOut.addActionListener(this::doCheckout);
        btnClear.addActionListener(e -> {
            txtCalls.setText("");
            txtResult.setText("");
            txtPatronId.requestFocus();
        });
    }

    private void loadSessionUser() {
        // Show username (or "Admin") read-only
        String name = Session.currentUsername;
        if (name == null || name.isBlank()) name = "Admin";
        txtEmployee.setText(name);
        txtEmployee.setCaretPosition(0);
        txtEmployee.setEditable(false);
        // Make text field look disabled but readable
        txtEmployee.setBackground(UIManager.getColor("TextField.inactiveBackground"));
    }

    private List<String> parseCalls() {
        return Arrays.stream(txtCalls.getText().split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private void doCheckout(ActionEvent e) {
        String patronId = txtPatronId.getText().trim();
        List<String> calls = parseCalls();

        if (patronId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Patron ID", "Error", JOptionPane.ERROR_MESSAGE);
            txtPatronId.requestFocus();
            return;
        }
        if (calls.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter at least one CallNumber", "Error", JOptionPane.ERROR_MESSAGE);
            txtCalls.requestFocus();
            return;
        }

        try (Connection conn = DB.get()) {
            LoanTicketDAO dao = new LoanTicketDAO(conn);
            long userId = Session.currentUserId; // <-- dùng Session
            LoanTicketDAO.CheckoutResult res = dao.checkoutMany(calls, patronId, userId);

            StringBuilder sb = new StringBuilder();
            sb.append("CHECK-OUT SUCCESS\n");
            sb.append("PatronSysId : ").append(res.patronSysId).append("\n");
            sb.append("Items      : ").append(res.itemCount).append("\n");
            sb.append("IssueDate  : ").append(res.issueDate).append("\n");
            sb.append("DueDate    : ").append(res.dueDate).append("\n");

            if (res.checked != null && !res.checked.isEmpty()) {
                sb.append("Checked    : ").append(String.join(", ", res.checked)).append("\n");
            }
            if (res.skipped != null && !res.skipped.isEmpty()) {
                sb.append("Skipped    : ").append(String.join(", ", res.skipped)).append("\n");
            }

            txtResult.setText(sb.toString());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // quick runner for standalone test
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels()) {
                if (Objects.equals("Nimbus", i.getName())) { UIManager.setLookAndFeel(i.getClassName()); break; }
            }
        } catch (Exception ignore) {}

        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Check-out");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setContentPane(new CheckOutForm());
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
