package UI;

import Model.Employee;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class EmployeeForm extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private ArrayList<Employee> employees;

    public EmployeeForm() {
        setLayout(new BorderLayout());

        employees = TransactionMockData.getEmployees();

        String[] cols = {"Employee ID", "Name", "Address", "Phone", "Department"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        refreshTable();

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void refreshTable() {
        model.setRowCount(0);
        for (Employee e : employees) {
            model.addRow(new Object[]{
                    e.getEmployeeID(), e.getName(), e.getAddress(),
                    e.getPhoneNum(), e.getDepartment()
            });
        }
    }
}
