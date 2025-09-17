package UI;

import DAO.BorrowRecordDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Util.DB;
import DAO.LoanTicketDAO;
import Util.Session;
import java.sql.Connection;

public class OpenLoansDialog extends JDialog {
    private final BorrowRecordDAO dao = new BorrowRecordDAO();
    private final JTextField tfCall = new JTextField(10);
    private final JTextField tfPatron = new JTextField(12);
    private final JTextField tfTitle = new JTextField(12);
    private final JCheckBox cbOverdue = new JCheckBox("Overdue only");
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"CallNumber","Title","PatronID","PatronName","IssueDate","DueDate"}, 0) {
        @Override public boolean isCellEditable(int r,int c){ return false; }
    };
    private final JTable table = new JTable(model);
    private final boolean pickMode;

    public OpenLoansDialog(Window owner, boolean pickMode) {
        super(owner, pickMode?"Pick Open Loans":"Current Loans", ModalityType.APPLICATION_MODAL);
        this.pickMode = pickMode;
        buildUI();
        load();
        setSize(820, 480);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        setLayout(new BorderLayout(8,8));
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btSearch = new JButton("Search");
        JButton btReset  = new JButton("Reset");
        north.add(new JLabel("Call:")); north.add(tfCall);
        north.add(new JLabel("Patron:")); north.add(tfPatron);
        north.add(new JLabel("Title:")); north.add(tfTitle);
        north.add(cbOverdue);
        north.add(btSearch); north.add(btReset);
        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btClose = new JButton("Close");
        south.add(btClose);
        if (pickMode) {
            JButton btAdd = new JButton("Add Selected");
            south.add(btAdd);
            btAdd.addActionListener(e -> doAdd());
        } else {
            JButton btReturn = new JButton("Return Selected");
            JButton btExport = new JButton("Export CSV...");
            south.add(btReturn);
            south.add(btExport);
            btReturn.addActionListener(e -> doReturn());
            btExport.addActionListener(e -> exportCsv());
        }
        add(south, BorderLayout.SOUTH);

        btSearch.addActionListener(e -> load());
        btReset.addActionListener(e -> { tfCall.setText(""); tfPatron.setText(""); tfTitle.setText(""); cbOverdue.setSelected(false); load(); });
        btClose.addActionListener(e -> dispose());

        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setAutoCreateRowSorter(true);
    }

    private void load() {
        try {
            model.setRowCount(0);
            List<Map<String,Object>> rows = dao.searchOpenLoans(nv(tfCall.getText()), nv(tfPatron.getText()), nv(tfTitle.getText()), cbOverdue.isSelected(), 1, 100);
            for (Map<String,Object> r : rows) {
                model.addRow(new Object[]{ r.get("CallNumber"), r.get("Title"), r.get("PatronID"), r.get("PatronName"), r.get("IssueDate"), r.get("DueDate") });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doAdd() {
        int[] sel = table.getSelectedRows();
        if (sel==null || sel.length==0) { JOptionPane.showMessageDialog(this, "Select at least one row"); return; }
        List<String> calls = new ArrayList<>();
        for (int r : sel) {
            int mr = table.convertRowIndexToModel(r);
            calls.add((String) model.getValueAt(mr,0));
        }
        selectedCalls = calls;
        dispose();
    }

    private void doReturn() {
        int[] sel = table.getSelectedRows();
        if (sel==null || sel.length==0) { JOptionPane.showMessageDialog(this, "Select at least one row"); return; }
        List<String> calls = new ArrayList<>();
        for (int r : sel) { int mr = table.convertRowIndexToModel(r); calls.add((String) model.getValueAt(mr,0)); }
        try (Connection c = DB.get()) {
            LoanTicketDAO dao = new LoanTicketDAO(c);
            long userId = Session.currentUserId==null?0:Session.currentUserId;
            LoanTicketDAO.CheckinResult res = dao.checkinMany(calls, userId);
            String msg = "Returned: "+res.itemCount + (res.skipped.isEmpty()?"":" | Skipped: "+String.join(", ", res.skipped));
            JOptionPane.showMessageDialog(this, msg);
            load(); // refresh list
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("current-loans.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fc.getSelectedFile()), StandardCharsets.UTF_8))) {
            // header
            for (int c=0;c<model.getColumnCount();c++) { if (c>0) pw.print(','); pw.print('"'); pw.print(model.getColumnName(c)); pw.print('"'); }
            pw.println();
            for (int r=0;r<model.getRowCount();r++) {
                for (int c=0;c<model.getColumnCount();c++) {
                    if (c>0) pw.print(',');
                    String s = String.valueOf(model.getValueAt(r,c)).replace("\"","\"\"");
                    pw.print('"'); pw.print(s); pw.print('"');
                }
                pw.println();
            }
            pw.flush();
            JOptionPane.showMessageDialog(this, "Exported");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String nv(String s){ return (s==null||s.isBlank())?null:s.trim(); }

    private List<String> selectedCalls = null;
    public static List<String> pick(Window owner) {
        OpenLoansDialog d = new OpenLoansDialog(owner, true);
        d.setVisible(true);
        return d.selectedCalls;
    }
    public static void showView(Window owner) {
        new OpenLoansDialog(owner, false).setVisible(true);
    }
}

