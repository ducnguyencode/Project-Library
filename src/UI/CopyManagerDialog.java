// UI/CopyManagerDialog.java  (phiên bản hỗ trợ multi-select + bulk actions)
package UI;

import DAO.BookCopyDAO;
import Model.BookCopy;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CopyManagerDialog {

    /** Mở dialog theo BookID (tự load danh sách). */
    public static void show(Component parent, long bookId, BookCopyDAO dao) {
        Window owner = SwingUtilities.getWindowAncestor(parent);
        JDialog dlg = new JDialog(owner, "Copies", Dialog.ModalityType.APPLICATION_MODAL);

        DefaultTableModel m = new DefaultTableModel(
                new Object[]{"CopyID","Seq","CallNumber","Available","Status","Shelf"},0) {
            public boolean isCellEditable(int r,int c){ return false; }
        };
        JTable t = new JTable(m);
        t.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // cho phép chọn nhiều

        Runnable reload = () -> {
            try {
                m.setRowCount(0);
                List<BookCopy> copies = new DAO.BookCopyDAO().findByBook(bookId);
                for (BookCopy c : copies) {
                    m.addRow(new Object[]{c.copyId,c.copySeq,c.callNumber,c.isAvailable,c.status,c.shelfLocation});
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        };
        reload.run();

        // Buttons
        JButton btAdd = new JButton("Add...");
        JButton btAvail=new JButton("Available");
        JButton btLost=new JButton("Lost");
        JButton btDam=new JButton("Damaged");
        JButton btRet=new JButton("Retire");
        JButton btDel=new JButton("Delete");

        btAdd.addActionListener(e -> {
            String s = JOptionPane.showInputDialog(dlg, "How many copies to add?", "1");
            if (s == null) return;
            s = s.trim();
            int n;
            try { n = Integer.parseInt(s); if (n<=0) throw new NumberFormatException(); }
            catch (Exception ex){ msg("Invalid number"); return; }

            int ok=0, fail=0;
            for (int i=0;i<n;i++){
                try { dao.insertAuto(bookId); ok++; }
                catch (Exception ex){ fail++; }
            }
            msg("Added: "+ok+(fail>0?(" | Failed: "+fail):""));
            reload.run();
        });

        btAvail.addActionListener(e -> bulkChange(dao, t, m, "AVAILABLE", reload));
        btLost .addActionListener(e -> bulkChange(dao, t, m, "LOST",      reload));
        btDam  .addActionListener(e -> bulkChange(dao, t, m, "DAMAGED",   reload));
        btRet  .addActionListener(e -> bulkChange(dao, t, m, "RETIRED",   reload));
        btDel  .addActionListener(e -> bulkDelete(dao, t, m, reload));

        JPanel south=new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btAdd);
        south.add(btAvail); south.add(btLost); south.add(btDam); south.add(btRet); south.add(btDel);

        dlg.add(new JScrollPane(t)); dlg.add(south,BorderLayout.SOUTH);
        dlg.setSize(760,420); dlg.setLocationRelativeTo(parent); dlg.setVisible(true);
    }

    // ===== helpers =====

    private static void bulkChange(BookCopyDAO dao, JTable t, DefaultTableModel m,
                                   String status, Runnable reload) {
        int[] sel = t.getSelectedRows();
        if (sel == null || sel.length == 0) { msg("Select one or more rows"); return; }
        int ok=0, fail=0;
        for (int i : sel) {
            long id = (long)m.getValueAt(i,0);
            try { dao.changeStatus(id, status); ok++; }
            catch (Exception ex){ fail++; }
        }
        msg("Updated: "+ok+(fail>0?(" | Failed: "+fail):""));
        reload.run();
    }

    private static void bulkDelete(BookCopyDAO dao, JTable t, DefaultTableModel m, Runnable reload){
        int[] sel = t.getSelectedRows();
        if (sel == null || sel.length == 0) { msg("Select one or more rows"); return; }
        if (JOptionPane.showConfirmDialog(t, "Delete "+sel.length+" selected copy(ies)?",
                "Confirm", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        // xóa từ dòng lớn xuống nhỏ để không lệch chỉ số
        int ok=0, fail=0;
        for (int k = sel.length-1; k>=0; k--) {
            int row = sel[k];
            long id = (long)m.getValueAt(row,0);
            try {
                boolean done = dao.deleteIfNoOpenLoan(id);
                if (done) ok++; else fail++;
            } catch (Exception ex){ fail++; }
        }
        msg("Deleted: "+ok+(fail>0?(" | Blocked/Failed: "+fail):""));
        reload.run();
    }

    private static void msg(String s){ JOptionPane.showMessageDialog(null,s); }
}
