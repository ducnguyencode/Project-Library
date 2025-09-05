package UI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class CheckInForm extends JPanel {
    private JTextField txtCallNumber;
    private JButton btnFind;
    private JTable tblOpenBorrows;
    private JCheckBox chkWaiveFine;
    private JTextField txtNote;
    private JButton btnCheckIn;
    private JTextArea txtResult;

    public CheckInForm() {
        setLayout(new BorderLayout(10, 10));

        JPanel pnlNorth = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlNorth.add(new JLabel("Book Call Number:"));
        txtCallNumber = new JTextField(15);
        pnlNorth.add(txtCallNumber);
        btnFind = new JButton("Find");
        pnlNorth.add(btnFind);
        add(pnlNorth, BorderLayout.NORTH);

        String[] cols = {"Copy ID","Title","Reader","Borrow Date","Due Date","Days Late","Fine"};
        tblOpenBorrows = new JTable(new DefaultTableModel(cols,0));
        JScrollPane scrollTable = new JScrollPane(tblOpenBorrows);
        add(scrollTable, BorderLayout.CENTER);

        JPanel pnlSouth = new JPanel(new BorderLayout(5, 5));
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        chkWaiveFine = new JCheckBox("Waive fine");
        pnlActions.add(chkWaiveFine);
        pnlActions.add(new JLabel("Note:"));
        txtNote = new JTextField(15);
        pnlActions.add(txtNote);
        btnCheckIn = new JButton("Check In");
        btnCheckIn.setEnabled(false);
        pnlActions.add(btnCheckIn);
        pnlSouth.add(pnlActions, BorderLayout.NORTH);

        txtResult = new JTextArea(4, 50);
        txtResult.setEditable(false);
        pnlSouth.add(new JScrollPane(txtResult), BorderLayout.CENTER);

        add(pnlSouth, BorderLayout.SOUTH);

        btnFind.addActionListener(e -> doFind());
        btnCheckIn.addActionListener(e -> doCheckIn());
        tblOpenBorrows.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) btnCheckIn.setEnabled(tblOpenBorrows.getSelectedRow() >= 0);
        });

        tblOpenBorrows.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblOpenBorrows.getColumnModel().getColumn(1).setPreferredWidth(150);
        tblOpenBorrows.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblOpenBorrows.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblOpenBorrows.getColumnModel().getColumn(4).setPreferredWidth(100);
        tblOpenBorrows.getColumnModel().getColumn(5).setPreferredWidth(80);
        tblOpenBorrows.getColumnModel().getColumn(6).setPreferredWidth(80);
    }

    private void doFind() {
        String callNo = txtCallNumber.getText().trim();
        if (callNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Call Number!");
            return;
        }
        DefaultTableModel m = (DefaultTableModel) tblOpenBorrows.getModel();
        m.setRowCount(0);
        if (callNo.equalsIgnoreCase("002-0001")) {
            m.addRow(new Object[]{"C124","Máy lọc","R088","2025-09-01","2025-09-03", "2","10000"});
            m.addRow(new Object[]{"C125","Máy lọc","R091","2025-09-02","2025-09-05", "0","0"});
        } else if (callNo.equalsIgnoreCase("001-0002")) {
            m.addRow(new Object[]{"C123","Duc Nguyen 2","R045","2025-09-03","2025-09-05", "0","0"});
        } else {
            JOptionPane.showMessageDialog(this, "No open borrows for this Call Number.");
        }
        txtResult.append("Find: " + callNo + "\n");
    }

    private void doCheckIn() {
        int r = tblOpenBorrows.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Please select a borrow record!");
            return;
        }
        String copyId = String.valueOf(tblOpenBorrows.getValueAt(r,0));
        String title  = String.valueOf(tblOpenBorrows.getValueAt(r,1));
        String reader = String.valueOf(tblOpenBorrows.getValueAt(r,2));
        boolean waived = chkWaiveFine.isSelected();
        String note = txtNote.getText().trim();
        txtResult.append("Checked In: " + copyId + " - " + title + ", Reader " + reader +
                ", Waive=" + waived + (note.isEmpty() ? "" : ", Note=" + note) +
                ", At " + LocalDate.now() + "\n");
        ((DefaultTableModel) tblOpenBorrows.getModel()).removeRow(r);
        btnCheckIn.setEnabled(false);
        txtCallNumber.requestFocusInWindow();
    }
}
