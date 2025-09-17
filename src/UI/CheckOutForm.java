package UI;

import DAO.LoanTicketDAO;
import DAO.BorrowRecordDAO;
import DAO.LibSettingsDAO;
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
    private JButton    btnLoadCart;
    private JButton    btnPaste;
    private JButton    btnPickPatron;
    private JLabel     lblQuota;         // remaining borrow slots for patron

    public CheckOutForm() {
        buildUI();
        bindEvents();
        loadSessionUser();
        if (Session.cartSize() > 0) {
            loadCartIntoText();
        }
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

        // Patron ID (input) + picker
        g.gridx = 0; g.gridy = 1; g.fill = GridBagConstraints.NONE; g.weightx = 0;
        top.add(new JLabel("Patron ID:"), g);
        JPanel patronLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        txtPatronId = new JTextField(18);
        btnPickPatron = new JButton("Pick...");
        lblQuota = new JLabel(" ");
        lblQuota.setForeground(new Color(0, 102, 0));
        patronLine.add(txtPatronId);
        patronLine.add(btnPickPatron);
        patronLine.add(Box.createHorizontalStrut(10));
        patronLine.add(lblQuota);
        g.gridx = 1; g.gridy = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        top.add(patronLine, g);

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
        btnLoadCart = new JButton("Load Cart");
        btnPaste = new JButton("Paste");
        JLabel hint = new JLabel("Tip: scan barcodes; Enter = new line, Ctrl+Enter = Check Out");
        hint.setFont(hint.getFont().deriveFont(hint.getFont().getSize2D()-1f));
        btns.add(btnCheckOut);
        btns.add(btnClear);
        btns.add(btnLoadCart);
        btns.add(btnPaste);
        g.gridx = 1; g.gridy = 3; g.weightx = 0; g.weighty = 0; g.fill = GridBagConstraints.NONE;
        top.add(btns, g);
        g.gridx = 1; g.gridy = 4; g.weightx = 0; g.weighty = 0; g.fill = GridBagConstraints.NONE;
        top.add(hint, g);

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

        // Calls area: Ctrl+Enter = checkout; Enter = newline (default)
        txtCalls.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
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
        btnLoadCart.addActionListener(e -> loadCartIntoText());

        // Paste from clipboard to calls
        btnPaste.addActionListener(e -> {
            try {
                var cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                var data = cb.getData(java.awt.datatransfer.DataFlavor.stringFlavor);
                if (data != null) {
                    String s = data.toString().replace("\r\n", "\n").replace('\r','\n');
                    if (!txtCalls.getText().isBlank()) s = "\n" + s;
                    txtCalls.append(s);
                    txtCalls.requestFocus();
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });

        // Patron picker
        btnPickPatron.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            String sel = PatronPickerDialog.pick(w);
            if (sel != null) { txtPatronId.setText(sel); updateQuota(); txtCalls.requestFocus(); }
        });
        // update quota when leaving Patron field
        txtPatronId.addFocusListener(new java.awt.event.FocusAdapter(){
            @Override public void focusLost(java.awt.event.FocusEvent e){ updateQuota(); }
        });
    }

    private void updateQuota() {
        String patronId = txtPatronId.getText().trim();
        if (patronId.isEmpty()) { lblQuota.setText(" "); return; }
        SwingUtilities.invokeLater(() -> {
            try {
                BorrowRecordDAO br = new BorrowRecordDAO();
                int open = br.listOpenByPatron(patronId).size();
                var settings = new LibSettingsDAO().getLatest();
                int max = (settings!=null && settings.containsKey("MaxBooksPerPatron"))
                        ? Integer.parseInt(String.valueOf(settings.get("MaxBooksPerPatron"))) : 0;
                int remain = Math.max(0, max - open);
                lblQuota.setText("Remaining: "+remain+" (Open: "+open+" / Max: "+max+")");
                lblQuota.setForeground(remain>0? new Color(0,102,0) : new Color(178,34,34));
            } catch (Exception ex) {
                lblQuota.setText(" ");
            }
        });
    }

    private void loadCartIntoText() {
        List<String> items = Session.cartItems();
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty");
        }
        txtCalls.setText(String.join("\n", items));
        if (!items.isEmpty()) txtCalls.setCaretPosition(txtCalls.getText().length());
    }

    private void loadSessionUser() {
        // Show username (or "Admin") read-only
        String name = Session.currentUsername;
        if (name == null || name.isBlank()) name = "Admin";
        txtEmployee.setText(name);
        // prefill last patron id if available
        if (Session.getLastPatronId() != null) txtPatronId.setText(Session.getLastPatronId());
        updateQuota();
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
            // remember this patron for next time
            Session.setLastPatronId(patronId);

            StringBuilder sb = new StringBuilder();
            sb.append("CHECK-OUT SUCCESS\n");
            sb.append("PatronSysId : ").append(res.patronSysId).append("\n");
            sb.append("Items      : ").append(res.itemCount).append("\n");
            sb.append("IssueDate  : ").append(res.issueDate).append("\n");
            sb.append("DueDate    : ").append(res.dueDate).append("\n");

            if (res.checked != null && !res.checked.isEmpty()) {
                sb.append("Checked    : ").append(String.join(", ", res.checked)).append("\n");
                // remove checked items from cart
                for (String c : res.checked) Session.cartRemove(c);
            }
            if (res.skipped != null && !res.skipped.isEmpty()) {
                sb.append("Skipped    : ").append(String.join(", ", res.skipped)).append("\n");
            }
            sb.append("Cart left  : ").append(Session.cartSize()).append(" item(s)\n");

            txtResult.setText(sb.toString());
            updateQuota();
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
