// UI/PatronForm.java
package UI;

import DAO.PatronDAO;
import Model.Patron;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class PatronForm extends JPanel {
    private final JTextField tfPatronId = new JTextField(10);
    private final JTextField tfName     = new JTextField(14);

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"PatronSysID","PatronID","Name","Phone","Email","Active","Open","Total Fees"},0) {
        public boolean isCellEditable(int r,int c){ return false; }
    };
    private final JTable tbl = new JTable(model);

    private final JButton btSearch = new JButton("Search");
    private final JButton btReset  = new JButton("Reset");
    private final JButton btAdd    = new JButton("Add");
    private final JButton btEdit   = new JButton("Edit");
    private final JButton btDelete = new JButton("Delete");
    private final JButton btView   = new JButton("View Loans");

    private final PatronDAO dao = new PatronDAO();

    public PatronForm() {
        setLayout(new BorderLayout(8,8));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Patron ID:")); top.add(tfPatronId);
        top.add(new JLabel("Name:"));      top.add(tfName);
        top.add(btSearch); top.add(btReset);
        add(top, BorderLayout.NORTH);

        tbl.getColumnModel().getColumn(0).setPreferredWidth(90);
        add(new JScrollPane(tbl), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btAdd); bottom.add(btEdit); bottom.add(btDelete);
        bottom.add(new JLabel(" | ")); bottom.add(btView);
        add(bottom, BorderLayout.SOUTH);

        btSearch.addActionListener(e -> load());
        btReset .addActionListener(e -> { tfPatronId.setText(""); tfName.setText(""); load(); });
        btAdd   .addActionListener(e -> onAdd());
        btEdit  .addActionListener(e -> onEdit());
        btDelete.addActionListener(e -> onDelete());
        btView  .addActionListener(e -> onViewLoans());

        load();
    }

    private void load() {
        try {
            model.setRowCount(0);
            List<Map<String,Object>> rows = dao.searchWithSummary(nv(tfPatronId.getText()), nv(tfName.getText()), 1, 20);
            for (Map<String,Object> r : rows) {
                model.addRow(new Object[]{
                        r.get("PatronSysID"), r.get("PatronID"), r.get("Name"),
                        r.get("Phone"), r.get("Email"),
                        r.get("IsActive"), r.get("OpenLoans"), r.get("TotalFees")
                });
            }
        } catch (Exception ex) { show(ex); }
    }

    private void onAdd() {
        Patron p = PatronDialog.show(null);
        if (p == null) return;
        try { long id = dao.insert(p); JOptionPane.showMessageDialog(this, "Created PatronSysID="+id); load(); }
        catch (Exception ex){ show(ex); }
    }

    private void onEdit() {
        int r = tbl.getSelectedRow(); if (r<0){ msg("Select a patron"); return; }
        long id = (long) model.getValueAt(r,0);
        try {
            Patron cur = dao.get(id);
            Patron p = PatronDialog.show(cur);
            if (p==null) return;
            dao.update(p);
            load();
        } catch (Exception ex){ show(ex); }
    }

    private void onDelete() {
        int r = tbl.getSelectedRow(); if (r<0){ msg("Select a patron"); return; }
        long id = (long) model.getValueAt(r,0);
        if (JOptionPane.showConfirmDialog(this,"Delete this patron?","Confirm",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            boolean ok = dao.deleteIfNoOpenLoans(id);
            msg(ok? "Deleted" : "Cannot delete: patron has open loans.");
            if (ok) load();
        } catch (Exception ex){ show(ex); }
    }

    private void onViewLoans() {
        int r = tbl.getSelectedRow(); if (r<0){ msg("Select a patron"); return; }
        long id = (long) model.getValueAt(r,0);
        try {
            List<Map<String,Object>> rows = dao.borrowDetails(id);
            PatronLoansDialog.show(this, rows);
        } catch (Exception ex){ show(ex); }
    }

    private static String nv(String s){ return (s==null || s.isBlank())? null : s.trim(); }
    private void show(Exception ex){ ex.printStackTrace(); JOptionPane.showMessageDialog(this, ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE); }
    private void msg(String s){ JOptionPane.showMessageDialog(this, s); }
}
