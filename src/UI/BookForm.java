// UI/BookForm.java
package UI;

import DAO.BookDAO;
import DAO.BookCopyDAO;
import DAO.SubjectDAO;
import Model.Books;
import Model.BookCopy;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.*;
import javax.swing.SwingWorker;

public class BookForm extends JPanel {

    private final JTextField tfIsbn   = new JTextField(10);
    private final JTextField tfAuthor = new JTextField(10);
    private final JTextField tfTitle  = new JTextField(12);

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"BookID","Cover","ISBN","Title","Author","Price","Total","Available"},0) {
        public boolean isCellEditable(int r,int c){ return false; }
    };
    private final JTable tbl = new JTable(model) {
        @Override public Class<?> getColumnClass(int column) { return column==1 ? Icon.class : Object.class; }
    };

    private final Map<String, ImageIcon> coverCache = new HashMap<>();
    private static final int COVER_W = 48, COVER_H = 64;

    private final JButton btSearch  = new JButton("Search");
    private final JButton btReset   = new JButton("Reset");
    private final JButton btAdd     = new JButton("Add Book");
    private final JButton btEdit    = new JButton("Edit Book");
    private final JButton btDelete  = new JButton("Delete Book");
    private final JButton btAddCopy = new JButton("Add Copy");
    private final JButton btCopies  = new JButton("View Copies");
    private final JButton btAvail   = new JButton("Mark Available");
    private final JButton btLost    = new JButton("Mark Lost");
    private final JButton btDamage  = new JButton("Mark Damaged");
    private final JButton btRetire  = new JButton("Retire");

    private final BookDAO bookDAO = new BookDAO();
    private final BookCopyDAO copyDAO = new BookCopyDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();

    public BookForm() {
        setLayout(new BorderLayout(8,8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("ISBN:"));   top.add(tfIsbn);
        top.add(new JLabel("Author:")); top.add(tfAuthor);
        top.add(new JLabel("Title:"));  top.add(tfTitle);
        top.add(btSearch); top.add(btReset);
        add(top, BorderLayout.NORTH);

        tbl.setRowHeight(Math.max(tbl.getRowHeight(), COVER_H));
        tbl.getColumnModel().getColumn(0).setPreferredWidth(60);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(70);
        tbl.getColumnModel().getColumn(2).setPreferredWidth(110);
        add(new JScrollPane(tbl), BorderLayout.CENTER);

        JPopupMenu rowMenu = makeRowMenu();
        tbl.addMouseListener(new MouseAdapter() {
            private void maybeShow(MouseEvent e){
                if (!e.isPopupTrigger()) return;
                int row = tbl.rowAtPoint(e.getPoint());
                if (row >= 0) { tbl.setRowSelectionInterval(row,row); rowMenu.show(tbl, e.getX(), e.getY()); }
            }
            @Override public void mousePressed (MouseEvent e){ maybeShow(e); }
            @Override public void mouseReleased(MouseEvent e){ maybeShow(e); }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btAdd); bottom.add(btEdit); bottom.add(btDelete);
        bottom.add(new JLabel(" | "));
        bottom.add(btAddCopy); bottom.add(btCopies);
        bottom.add(btAvail);
        bottom.add(btLost); bottom.add(btDamage); bottom.add(btRetire);
        add(bottom, BorderLayout.SOUTH);

        btSearch.addActionListener(e -> load());
        btReset.addActionListener(e -> { tfIsbn.setText(""); tfAuthor.setText(""); tfTitle.setText(""); load(); });

        btAdd.addActionListener(e -> onAdd());
        btEdit.addActionListener(e -> onEdit());
        btDelete.addActionListener(e -> onDelete());
        btAddCopy.addActionListener(e -> onAddCopy());
        btCopies.addActionListener(e -> onViewCopies());

        btAvail.addActionListener(e -> changeStatus("AVAILABLE"));
        btLost .addActionListener(e -> changeStatus("LOST"));
        btDamage.addActionListener(e -> changeStatus("DAMAGED"));
        btRetire.addActionListener(e -> changeStatus("RETIRED"));

        load();
    }

    private void load() {
        try {
            model.setRowCount(0);
            var rows = bookDAO.searchWithAvailability(nv(tfIsbn.getText()), nv(tfAuthor.getText()), nv(tfTitle.getText()), 1, 20);
            for (var r : rows) addRowAndLoadCover(r);
        } catch (Exception ex) { show(ex); }
    }

    private void onAdd() {
        Books b = BookDialog.show(null, subjectDAO);
        if (b == null) return;
        try { long id = bookDAO.insertAuto(b); JOptionPane.showMessageDialog(this, "Created BookID=" + id); load(); }
        catch (Exception ex) { show(ex); }
    }

    private void onEdit() {
        int r = tbl.getSelectedRow(); if (r<0){ msg("Select a book"); return; }
        long bookId = (long)model.getValueAt(r,0);
        String oldTitle  = (String)model.getValueAt(r,3);
        String oldAuthor = (String)model.getValueAt(r,4);
        Books b = BookDialog.show(bookId, subjectDAO); if (b==null) return;
        boolean rename = !oldTitle.equals(b.title) || !oldAuthor.equals(b.author);
        try { bookDAO.update(b, rename); load(); } catch (Exception ex){ show(ex); }
    }

    private void onDelete() {
        int r = tbl.getSelectedRow(); if (r<0){ msg("Select a book"); return; }
        long id = (long)model.getValueAt(r,0);
        if (JOptionPane.showConfirmDialog(this,"Delete this book?","Confirm",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            boolean ok = bookDAO.deleteIfNoCopies(id);
            if (!ok) msg("Cannot delete: book still has copies");
            load();
        } catch (Exception ex){ show(ex); }
    }

    private void onAddCopy() {
        int r = tbl.getSelectedRow(); if (r<0){ msg("Select a book"); return; }
        long bookId = (long)model.getValueAt(r,0);
        String s = JOptionPane.showInputDialog(this, "How many copies to add?", "1");
        if (s == null) return;
        int n; try { n = Integer.parseInt(s.trim()); if (n<=0) throw new NumberFormatException(); }
        catch (Exception ex){ msg("Invalid number"); return; }

        int ok = 0, fail = 0;
        for (int i=0;i<n;i++) { try { copyDAO.insertAuto(bookId); ok++; } catch (Exception ex){ fail++; } }
        msg("Added: "+ok+(fail>0?(" | Failed: "+fail):"")); load();
    }

    private void onViewCopies() {
        int r = tbl.getSelectedRow(); if (r<0){ msg("Select a book"); return; }
        long bookId = (long)model.getValueAt(r,0);
        try { CopyManagerDialog.show(this, bookId, copyDAO); load(); } catch (Exception ex){ show(ex); }
    }

    private void changeStatus(String status) {
        int r = tbl.getSelectedRow(); if (r<0){ msg("Select a book"); return; }
        long bookId = (long)model.getValueAt(r,0);
        try {
            java.util.List<BookCopy> list = new DAO.BookCopyDAO().findByBook(bookId);
            Model.BookCopy chosen = CopyPicker.pick(this, list);
            if (chosen==null) return;
            copyDAO.changeStatus(chosen.copyId, status);
            load();
        } catch (Exception ex){ show(ex); }
    }

    private JPopupMenu makeRowMenu() {
        JPopupMenu pm = new JPopupMenu();
        JMenuItem miEdit   = new JMenuItem("Edit Book");
        JMenuItem miDelete = new JMenuItem("Delete Book");
        JMenuItem miAdd    = new JMenuItem("Add Copy");
        JMenuItem miView   = new JMenuItem("View Copies");
        JMenuItem miAvail  = new JMenuItem("Mark Available");
        JMenuItem miLost   = new JMenuItem("Mark Lost");
        JMenuItem miDam    = new JMenuItem("Mark Damaged");
        JMenuItem miRet    = new JMenuItem("Retire");
        miEdit.addActionListener(e -> onEdit());
        miDelete.addActionListener(e -> onDelete());
        miAdd.addActionListener(e -> onAddCopy());
        miView.addActionListener(e -> onViewCopies());
        miAvail.addActionListener(e -> changeStatus("AVAILABLE"));
        miLost.addActionListener(e -> changeStatus("LOST"));
        miDam.addActionListener(e -> changeStatus("DAMAGED"));
        miRet.addActionListener(e -> changeStatus("RETIRED"));
        pm.add(miEdit); pm.add(miDelete); pm.addSeparator();
        pm.add(miAdd); pm.add(miView); pm.addSeparator();
        pm.add(miAvail); pm.add(miLost); pm.add(miDam); pm.add(miRet);
        return pm;
    }

    // 👉👉 Method cần cho sidebar “Search by CallNumber”
    public void searchBook(String callNumber) {
        try {
            model.setRowCount(0);
            var rows = bookDAO.searchByCallNumberWithAvailability(nv(callNumber), 1, 20);
            for (var r : rows) addRowAndLoadCover(r); // vẫn tải Cover
        } catch (Exception ex) { show(ex); }
    }

    private static String nv(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
    private void show(Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    private void msg(String s) { JOptionPane.showMessageDialog(this, s); }

    private void addRowAndLoadCover(Map<String,Object> r) {
        model.addRow(new Object[]{ r.get("BookID"), null,
                r.get("ISBN"), r.get("Title"), r.get("Author"), r.get("Price"),
                r.get("TotalCopies"), r.get("AvailableCopies")});
        int row = model.getRowCount()-1;
        loadCoverAsync(row, (String) r.get("ImageURL"));
    }

    private void loadCoverAsync(int row, String url) {
        if (url == null || url.isBlank()) return;
        ImageIcon cached = coverCache.get(url);
        if (cached != null) { model.setValueAt(cached, row, 1); return; }
        new SwingWorker<ImageIcon,Void>(){
            @Override protected ImageIcon doInBackground() {
                try {
                    BufferedImage img = url.matches("(?i)^https?://.*") ? ImageIO.read(new URL(url)) : ImageIO.read(new File(url));
                    if (img == null) return null;
                    Image scaled = img.getScaledInstance(COVER_W, COVER_H, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                } catch (Exception ignore) { return null; }
            }
            @Override protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        coverCache.put(url, icon);
                        if (row >= 0 && row < model.getRowCount()) model.setValueAt(icon, row, 1);
                    }
                } catch (Exception ignore) {}
            }
        }.execute();
    }
}
