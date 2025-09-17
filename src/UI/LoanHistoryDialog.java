package UI;

import DAO.BorrowRecordDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.List;
import java.util.Map;

public class LoanHistoryDialog extends JDialog {
    private final BorrowRecordDAO dao = new BorrowRecordDAO();
    private final JTextField tfCall = new JTextField(10);
    private final JTextField tfPatron = new JTextField(12);
    private final JTextField tfTitle = new JTextField(12);
    private final JTextField tfFrom = new JTextField(8); // YYYY-MM-DD
    private final JTextField tfTo   = new JTextField(8); // YYYY-MM-DD

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"IssueDate","ReturnDate","CallNumber","Title","PatronID","PatronName","LateFee"}, 0) {
        @Override public boolean isCellEditable(int r,int c){ return false; }
    };
    private final JTable table = new JTable(model);

    public LoanHistoryDialog(Window owner) {
        super(owner, "Loan History", ModalityType.APPLICATION_MODAL);
        buildUI();
        load();
        setSize(900, 520);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        setLayout(new BorderLayout(8,8));
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btSearch = new JButton("Search");
        JButton btReset  = new JButton("Reset");
        north.add(new JLabel("From (YYYY-MM-DD):")); north.add(tfFrom);
        north.add(new JLabel("To:")); north.add(tfTo);
        north.add(new JLabel("Call:")); north.add(tfCall);
        north.add(new JLabel("Patron:")); north.add(tfPatron);
        north.add(new JLabel("Title:")); north.add(tfTitle);
        north.add(btSearch); north.add(btReset);
        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btExport = new JButton("Export CSV...");
        JButton btClose  = new JButton("Close");
        south.add(btExport); south.add(btClose);
        add(south, BorderLayout.SOUTH);

        btSearch.addActionListener(e -> load());
        btReset.addActionListener(e -> { tfFrom.setText(""); tfTo.setText(""); tfCall.setText(""); tfPatron.setText(""); tfTitle.setText(""); load(); });
        btClose.addActionListener(e -> dispose());
        btExport.addActionListener(e -> exportCsv());

        table.setAutoCreateRowSorter(true);
    }

    private void load() {
        try {
            model.setRowCount(0);
            Date from = parseDate(tfFrom.getText());
            Date to   = parseDate(tfTo.getText());
            List<Map<String,Object>> rows = dao.searchLoanHistory(nv(tfCall.getText()), nv(tfPatron.getText()), nv(tfTitle.getText()),
                    from, to, 1, 200);
            for (Map<String,Object> r : rows) {
                model.addRow(new Object[]{ r.get("IssueDate"), r.get("ReturnDate"), r.get("CallNumber"), r.get("Title"), r.get("PatronID"), r.get("PatronName"), r.get("LateFee") });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static Date parseDate(String s) {
        if (s==null) return null;
        s = s.trim(); if (s.isEmpty()) return null;
        try { return Date.valueOf(s); } catch (Exception ignore) { return null; }
    }

    private void exportCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("loan-history.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fc.getSelectedFile()), StandardCharsets.UTF_8))) {
            for (int c=0;c<model.getColumnCount();c++) { if (c>0) pw.print(','); pw.print('"'); pw.print(model.getColumnName(c)); pw.print('"'); }
            pw.println();
            for (int r=0;r<model.getRowCount();r++) {
                for (int c=0;c<model.getColumnCount();c++) {
                    if (c>0) pw.print(',');
                    String v = String.valueOf(model.getValueAt(r,c)).replace("\"","\"\"");
                    pw.print('"'); pw.print(v); pw.print('"');
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

    public static void showView(Window owner) { new LoanHistoryDialog(owner).setVisible(true); }
}

