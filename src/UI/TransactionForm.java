package UI;

import Model.Transaction;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class TransactionForm extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private ArrayList<Transaction> transactions;

    public TransactionForm() {
        setLayout(new BorderLayout());

        transactions = TransactionMockData.getTransactions();

        String[] cols = {"Transaction ID", "Employee", "Book", "Issue Date", "Due Date", "Return Date", "Fine"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);

        refreshTable();

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void refreshTable() {
        model.setRowCount(0);
        for (Transaction t : transactions) {
            model.addRow(new Object[]{
                    t.getTransactionID(),
                    t.getEmployee().getName(),
                    t.getBook().getTitle(),
                    t.getIssueDate(),
                    t.getDueDate(),
                    t.getReturnDate() == null ? "Not Returned" : t.getReturnDate(),
                    "$" + t.getFine()
            });
        }
    }
}
