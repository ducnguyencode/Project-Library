// UI/EmployeeDialog.java
package UI;

import Model.Employee;

import javax.swing.*;
import java.awt.*;

public class EmployeeDialog extends JDialog {
  private final JTextField tfId   = new JTextField(14);
  private final JTextField tfName = new JTextField(18);
  private final JTextField tfAddr = new JTextField(22);
  private final JTextField tfPhone= new JTextField(14);

  private Employee result = null;

  private EmployeeDialog(Window owner, Employee cur) {
    super(owner, (cur==null?"Add Employee":"Edit Employee"), ModalityType.APPLICATION_MODAL);

    JPanel form = new JPanel(new GridBagLayout());
    GridBagConstraints g = new GridBagConstraints();
    g.insets = new Insets(6,8,6,8); g.anchor = GridBagConstraints.WEST;
    int y=0;
    g.gridx=0; g.gridy=y; form.add(new JLabel("Employee ID:"), g);
    g.gridx=1;            form.add(tfId, g); y++;
    g.gridx=0; g.gridy=y; form.add(new JLabel("Name:"), g);
    g.gridx=1;            form.add(tfName, g); y++;
    g.gridx=0; g.gridy=y; form.add(new JLabel("Address:"), g);
    g.gridx=1;            form.add(tfAddr, g); y++;
    g.gridx=0; g.gridy=y; form.add(new JLabel("Phone:"), g);
    g.gridx=1;            form.add(tfPhone, g); y++;

    if (cur != null) {
      tfId.setText(cur.getEmployeeID());
      tfName.setText(cur.getName());
      tfAddr.setText(cur.getAddress());
      tfPhone.setText(cur.getPhoneNum());
    }

    JButton ok = new JButton("OK"), cancel = new JButton("Cancel");
    JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    south.add(ok); south.add(cancel);

    ok.addActionListener(e -> {
      String id = tfId.getText().trim();
      String nm = tfName.getText().trim();
      if (id.isBlank() || nm.isBlank()) {
        JOptionPane.showMessageDialog(this,"Employee ID and Name are required."); return;
      }
      Employee emp = new Employee(id, nm, tfAddr.getText().trim(), tfPhone.getText().trim());
      if (cur != null) emp.setSystemId(cur.getSystemId());
      result = emp;
      dispose();
    });
    cancel.addActionListener(e -> { result = null; dispose(); });

    getContentPane().add(form, BorderLayout.CENTER);
    getContentPane().add(south, BorderLayout.SOUTH);
    pack(); setLocationRelativeTo(owner);
  }

  /** Trả về Employee sau khi bấm OK; null nếu Cancel */
  public static Employee show(Window owner, Employee cur) {
    EmployeeDialog d = new EmployeeDialog(owner, cur);
    d.setVisible(true);
    return d.result;
  }

  /** Overload tiện dụng từ JPanel (không có Window) */
  public static Employee show(Employee cur) { return show(null, cur); }
}
