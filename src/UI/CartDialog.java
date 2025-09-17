package UI;

import Util.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.List;

public class CartDialog extends JDialog {
    private DefaultListModel<String> model;
    private JList<String> list;

    public CartDialog(Window owner) {
        super(owner, "Checkout Cart", ModalityType.APPLICATION_MODAL);
        buildUI();
        loadData();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        setLayout(new BorderLayout(8,8));
        model = new DefaultListModel<>();
        list = new JList<>(model);
        add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRemove = new JButton("Remove");
        JButton btnClear  = new JButton("Clear");
        JButton btnCopy   = new JButton("Copy Calls");
        JButton btnCheckout = new JButton("Check Out...");
        JButton btnClose  = new JButton("Close");
        btns.add(btnRemove); btns.add(btnClear); btns.add(btnCopy); btns.add(btnCheckout); btns.add(btnClose);
        add(btns, BorderLayout.SOUTH);

        btnRemove.addActionListener(e -> {
            List<String> sel = list.getSelectedValuesList();
            for (String s : sel) Session.cartRemove(s);
            loadData();
        });
        btnClear.addActionListener(e -> { Session.cartClear(); loadData(); });
        btnCopy.addActionListener(e -> {
            String text = String.join("\n", Session.cartItems());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
            JOptionPane.showMessageDialog(this, "Copied " + Session.cartSize() + " call(s) to clipboard.");
        });
        btnClose.addActionListener(e -> dispose());
        btnCheckout.addActionListener(e -> {
            JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), "Check Out", ModalityType.APPLICATION_MODAL);
            d.setContentPane(new CheckOutForm());
            d.pack();
            d.setLocationRelativeTo(this);
            d.setVisible(true);
        });
    }

    private void loadData() {
        model.clear();
        for (String s : Session.cartItems()) model.addElement(s);
        setTitle("Checkout Cart (" + model.size() + ")");
    }
}

