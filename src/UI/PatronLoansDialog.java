// UI/PatronLoansDialog.java
package UI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public final class PatronLoansDialog {
    public static void show(Component parent, List<Map<String,Object>> rows) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(parent), "Loans", Dialog.ModalityType.APPLICATION_MODAL);
        DefaultTableModel m = new DefaultTableModel(
                new Object[]{"RecordID","CallNumber","Title","IssueDate","DueDate","ReturnDate","LateFee","Deposit","Open"}, 0) {
            public boolean isCellEditable(int r,int c){ return false; }
        };
        JTable t = new JTable(m);
        for (Map<String,Object> r : rows) {
            m.addRow(new Object[]{
                    r.get("RecordID"), r.get("CallNumber"), r.get("Title"),
                    r.get("IssueDate"), r.get("DueDate"), r.get("ReturnDate"),
                    r.get("LateFee"), r.get("DepositAmount"), r.get("IsOpen")
            });
        }
        dlg.add(new JScrollPane(t));
        dlg.setSize(820, 420);
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }
}
