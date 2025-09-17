package UI;

import DAO.BookDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class BookDetailDialog extends JDialog {
    private final long bookId;

    public BookDetailDialog(Window owner, long bookId) {
        super(owner, "Book Detail", ModalityType.APPLICATION_MODAL);
        this.bookId = bookId;
        buildUI();
        loadData();
        setSize(720, 420);
        setLocationRelativeTo(owner);
    }

    private JLabel lblHeader = new JLabel();
    private JTable table;

    private void buildUI() {
        setLayout(new BorderLayout(8,8));
        lblHeader.setFont(lblHeader.getFont().deriveFont(Font.BOLD));
        add(lblHeader, BorderLayout.NORTH);

        table = new JTable(new DefaultTableModel(new Object[]{
                "CallNumber", "Status", "Available", "Borrower", "IssueDate", "DueDate"
        }, 0));
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton export = new JButton("Export CSV...");
        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        export.addActionListener(e -> exportCsv());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(export);
        south.add(close);
        add(south, BorderLayout.SOUTH);
    }

    private void exportCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("book-"+bookId+"-detail.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        java.io.File f = fc.getSelectedFile();
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(f), java.nio.charset.StandardCharsets.UTF_8))) {
            DefaultTableModel m = (DefaultTableModel) table.getModel();
            // header
            for (int c=0;c<m.getColumnCount();c++) {
                if (c>0) pw.print(',');
                pw.print('"'); pw.print(m.getColumnName(c)); pw.print('"');
            }
            pw.println();
            // rows
            for (int r=0;r<m.getRowCount();r++) {
                for (int c=0;c<m.getColumnCount();c++) {
                    if (c>0) pw.print(',');
                    Object val = m.getValueAt(r,c);
                    String s = val==null?"":val.toString();
                    s = s.replace("\"","\"\""); // escape quotes
                    pw.print('"'); pw.print(s); pw.print('"');
                }
                pw.println();
            }
            pw.flush();
            JOptionPane.showMessageDialog(this, "Exported: "+f.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export error: "+ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadData() {
        try {
            BookDAO dao = new BookDAO();
            Map<String,Object> h = dao.getHeader(bookId);
            if (h == null) return;
            lblHeader.setText(String.format("ISBN: %s  |  Title: %s  |  Author: %s",
                    h.get("ISBN"), h.get("Title"), h.get("Author")));

            List<Map<String,Object>> rows = dao.listCopyStatus(bookId);
            DefaultTableModel m = (DefaultTableModel) table.getModel();
            m.setRowCount(0);
            for (Map<String,Object> r : rows) {
                String borrower = r.get("PatronID") == null ? "" : (r.get("PatronID") + " - " + r.get("PatronName"));
                m.addRow(new Object[]{
                        r.get("CallNumber"), r.get("Status"), (Boolean) r.get("IsAvailable") ? "Yes" : "No",
                        borrower,
                        r.get("IssueDate"), r.get("DueDate")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

