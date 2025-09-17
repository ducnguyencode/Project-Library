package UI;

import DAO.PatronDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class PatronPickerDialog extends JDialog {
    private final PatronDAO dao = new PatronDAO();
    private final JTextField tfId = new JTextField(12);
    private final JTextField tfName = new JTextField(14);
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"PatronID","Name","Phone","Email","Active","OpenLoans","TotalFees"}, 0) {
        @Override public boolean isCellEditable(int r,int c){ return false; }
    };
    private final JTable table = new JTable(model);
    private String chosenPatronId = null;

    public PatronPickerDialog(Window owner) {
        super(owner, "Select Patron", ModalityType.APPLICATION_MODAL);
        buildUI();
        load();
        setSize(760, 420);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        setLayout(new BorderLayout(8,8));
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btSearch = new JButton("Search");
        JButton btReset  = new JButton("Reset");
        north.add(new JLabel("ID:")); north.add(tfId);
        north.add(new JLabel("Name:")); north.add(tfName);
        north.add(btSearch); north.add(btReset);
        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btSelect = new JButton("Select");
        JButton btClose  = new JButton("Close");
        south.add(btSelect); south.add(btClose);
        add(south, BorderLayout.SOUTH);

        btSearch.addActionListener(e -> load());
        btReset.addActionListener(e -> { tfId.setText(""); tfName.setText(""); load(); });
        btClose.addActionListener(e -> dispose());
        btSelect.addActionListener(e -> onSelect());
        table.addMouseListener(new java.awt.event.MouseAdapter(){
            @Override public void mouseClicked(java.awt.event.MouseEvent e){ if (e.getClickCount()==2) onSelect(); }
        });
    }

    private void load() {
        try {
            model.setRowCount(0);
            List<Map<String,Object>> rows = dao.searchWithSummary(nv(tfId.getText()), nv(tfName.getText()), 1, 20);
            for (Map<String,Object> r : rows) {
                model.addRow(new Object[]{ r.get("PatronID"), r.get("Name"), r.get("Phone"), r.get("Email"),
                        ((Boolean) r.get("IsActive"))?"Yes":"No", r.get("OpenLoans"), r.get("TotalFees")});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSelect() {
        int r = table.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Select a patron"); return; }
        chosenPatronId = (String) model.getValueAt(r, 0);
        dispose();
    }

    public String getChosenPatronId() { return chosenPatronId; }

    private static String nv(String s) { return (s==null||s.isBlank())?null:s.trim(); }

    public static String pick(Window owner) {
        PatronPickerDialog d = new PatronPickerDialog(owner);
        d.setVisible(true);
        return d.getChosenPatronId();
    }
}

