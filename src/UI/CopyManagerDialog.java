// UI/CopyManagerDialog.java
package UI;

import DAO.BookCopyDAO;
import Model.BookCopy;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CopyManagerDialog {

    /** Hiển thị & quản lý copies của 1 book. Gọi onRefresh.run() sau mỗi thay đổi để BookForm cập nhật ngay. */
    public static void show(Component parent, long bookId, BookCopyDAO dao, Runnable onRefresh) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(parent), "Copies", Dialog.ModalityType.APPLICATION_MODAL);

        DefaultTableModel m = new DefaultTableModel(
                new Object[]{"CopyID","Seq","CallNumber","Available","Status","Shelf"},0) {
            public boolean isCellEditable(int r,int c){ return false; }
        };
        JTable t = new JTable(m);

        // helper nạp lại bảng
        Runnable reload = () -> {
            try {
                m.setRowCount(0);
                List<BookCopy> copies = dao.findByBook(bookId);
                for (BookCopy c : copies) {
                    m.addRow(new Object[]{c.copyId,c.copySeq,c.callNumber,c.isAvailable,c.status,c.shelfLocation});
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        // --- Buttons ---
        JButton btAdd   = new JButton("Add…");
        JButton btAvail = new JButton("Available");
        JButton btLost  = new JButton("Lost");
        JButton btDam   = new JButton("Damaged");
        JButton btRet   = new JButton("Retire");
        JButton btDel   = new JButton("Delete");

        btAdd.addActionListener(e -> {
            String s = JOptionPane.showInputDialog(dlg, "Number of copies to add:", "1");
            if (s == null) return;
            int n;
            try { n = Integer.parseInt(s.trim()); }
            catch (Exception ex){ JOptionPane.showMessageDialog(dlg,"Invalid number"); return; }
            if (n <= 0) return;
            try {
                int created = dao.insertMany(bookId, n);
                JOptionPane.showMessageDialog(dlg, "Added " + created + " copies.");
                reload.run();
                if (onRefresh != null) onRefresh.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btAvail.addActionListener(e -> change(dao, t, "AVAILABLE", reload, onRefresh, dlg));
        btLost.addActionListener(e   -> change(dao, t, "LOST",       reload, onRefresh, dlg));
        btDam.addActionListener(e    -> change(dao, t, "DAMAGED",    reload, onRefresh, dlg));
        btRet.addActionListener(e    -> change(dao, t, "RETIRED",    reload, onRefresh, dlg));
        btDel.addActionListener(e    -> del   (dao, t, m, reload, onRefresh, dlg));

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btAdd);
        south.add(new JSeparator(SwingConstants.VERTICAL));
        south.add(btAvail); south.add(btLost); south.add(btDam); south.add(btRet); south.add(btDel);

        dlg.add(new JScrollPane(t));
        dlg.add(south, BorderLayout.SOUTH);
        dlg.setSize(740, 420);
        dlg.setLocationRelativeTo(parent);

        reload.run();
        dlg.setVisible(true);
    }

    private static void change(BookCopyDAO dao, JTable t, String status,
                               Runnable reload, Runnable onRefresh, Component parent){
        int r = t.getSelectedRow(); if (r < 0) { msg(parent,"Select a row"); return; }
        long id = ((Number) t.getValueAt(r,0)).longValue();
        String cur = String.valueOf(t.getValueAt(r,4));
        if ("RETIRED".equals(cur) && "AVAILABLE".equals(status)) {
            msg(parent,"A retired copy cannot be made available again.");
            return;
        }
        try {
            dao.changeStatus(id, status);
            reload.run();
            if (onRefresh != null) onRefresh.run();
        } catch (Exception ex) { msg(parent, ex.getMessage()); }
    }

    private static void del(BookCopyDAO dao, JTable t, DefaultTableModel m,
                            Runnable reload, Runnable onRefresh, Component parent){
        int r = t.getSelectedRow(); if (r < 0) { msg(parent,"Select a row"); return; }
        long id = ((Number) t.getValueAt(r,0)).longValue();
        try {
            boolean ok = dao.deleteIfNoOpenLoan(id);
            msg(parent, ok ? "Deleted" : "Cannot delete: copy has open loans.");
            if (ok) {
                reload.run();
                if (onRefresh != null) onRefresh.run();
            }
        } catch (Exception ex) { msg(parent, ex.getMessage()); }
    }

    private static void msg(Component p, String s){ JOptionPane.showMessageDialog(p, s); }
}
