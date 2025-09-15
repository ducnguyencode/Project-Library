package UI;

import DAO.EmployeeDAO;
import DAO.UserDAO;
import Model.Employee;
import Util.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;

public class EmployeeForm extends JPanel {
    private final JTextField tfId   = new JTextField(10);
    private final JTextField tfName = new JTextField(14);

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"SystemID","Employee ID","Name","Address","Phone"}, 0
    ){
        public boolean isCellEditable(int r,int c){ return false; }
    };

    private final JTable tbl = new JTable(model);

    private final JButton btSearch = new JButton("Search");
    private final JButton btReset  = new JButton("Reset");
    private final JButton btAdd    = new JButton("Add");
    private final JButton btEdit   = new JButton("Edit");
    private final JButton btDel    = new JButton("Delete");
    private final JButton btLogin  = new JButton("Create Login…"); // tạo tài khoản đăng nhập

    private final EmployeeDAO empDAO = new EmployeeDAO();
    private final UserDAO     userDAO = new UserDAO();

    public EmployeeForm(){
        setLayout(new BorderLayout(8,8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Employee ID:")); top.add(tfId);
        top.add(new JLabel("Name:"));        top.add(tfName);
        top.add(btSearch); top.add(btReset);
        add(top, BorderLayout.NORTH);

        add(new JScrollPane(tbl), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btAdd); bottom.add(btEdit); bottom.add(btDel);
        bottom.add(new JLabel(" | ")); bottom.add(btLogin);
        add(bottom, BorderLayout.SOUTH);

        // Hiển thị nút theo role
        boolean isAdmin = "ADMIN".equalsIgnoreCase(Session.currentRole);
        btAdd.setVisible(isAdmin);
        btEdit.setVisible(isAdmin);
        btDel.setVisible(isAdmin);
        btLogin.setVisible(isAdmin);

        btSearch.addActionListener(e -> load());
        btReset .addActionListener(e -> { tfId.setText(""); tfName.setText(""); load(); });
        btAdd   .addActionListener(e -> onAdd());
        btEdit  .addActionListener(e -> onEdit());
        btDel   .addActionListener(e -> onDelete());
        btLogin .addActionListener(e -> onCreateLogin());

        load();
    }

    private void load(){
        try{
            model.setRowCount(0);
            for (Map<String,Object> r: empDAO.search(nv(tfId.getText()), nv(tfName.getText()), 1, 50)){
                model.addRow(new Object[]{
                        r.get("SystemID"), r.get("EmployeeID"), r.get("Name"),
                        r.get("Address"),  r.get("Phone")
                });
            }
        }catch(Exception ex){ show(ex); }
    }

    private void onAdd(){
        if (!isAdmin()) { msg("Permission denied."); return; }
        Employee e = EmployeeDialog.show(null);
        if (e==null) return;
        try{
            long id = empDAO.insertAuto(e);
            JOptionPane.showMessageDialog(this,"Created SystemID="+id);
            load();
        } catch(Exception ex){ show(ex); }
    }

    private void onEdit(){
        if (!isAdmin()) { msg("Permission denied."); return; }
        int r = tbl.getSelectedRow(); if (r<0){ msg("Select an employee"); return; }
        long systemId = (long) model.getValueAt(r,0);
        try{
            Employee cur = empDAO.get(systemId);
            Employee e   = EmployeeDialog.show(cur);
            if (e==null) return;
            e.setSystemId(systemId);   // cập nhật đúng SystemID
            empDAO.update(e);
            load();
        }catch(Exception ex){ show(ex); }
    }

    private void onDelete(){
        if (!isAdmin()) { msg("Permission denied."); return; }
        int r = tbl.getSelectedRow(); if (r<0){ msg("Select an employee"); return; }
        long systemId = (long) model.getValueAt(r,0);
        if (JOptionPane.showConfirmDialog(this,"Delete this employee?","Confirm",
                JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION) return;
        try{
            boolean ok = empDAO.deleteIfNoUser(systemId);
            msg(ok? "Deleted" : "Cannot delete: an account still references this employee.");
            if (ok) load();
        }catch(Exception ex){ show(ex); }
    }

    /** Tạo tài khoản đăng nhập cho nhân viên đã chọn */
    private void onCreateLogin(){
        if (!isAdmin()) { msg("Permission denied."); return; }
        int r = tbl.getSelectedRow(); if (r<0){ msg("Select an employee"); return; }
        long systemId = (long) model.getValueAt(r,0);

        JTextField tfUser = new JTextField(14);
        JPasswordField pf  = new JPasswordField(14);
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"LIBRARIAN","ADMIN"});

        JPanel p = new JPanel(new GridLayout(0,2,6,6));
        p.add(new JLabel("Username:"));      p.add(tfUser);
        p.add(new JLabel("Temp password:")); p.add(pf);
        p.add(new JLabel("Role:"));          p.add(cbRole);

        if (JOptionPane.showConfirmDialog(this,p,"Create Login",
                JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION) return;

        try{
            String username = tfUser.getText().trim();
            String tempPw   = new String(pf.getPassword());
            String role     = (String) cbRole.getSelectedItem();
            userDAO.createForEmployee(systemId, username, tempPw, role);
            msg("Account created. Ask the staff to login and change the password.");
        }catch(Exception ex){ show(ex); }
    }

    private static String nv(String s){ return (s==null||s.isBlank())? null : s.trim(); }
    private void show(Exception ex){ ex.printStackTrace(); JOptionPane.showMessageDialog(this, ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE); }
    private void msg(String s){ JOptionPane.showMessageDialog(this, s); }
    private boolean isAdmin(){ return "ADMIN".equalsIgnoreCase(Session.currentRole); }
}
