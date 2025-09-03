package UI;

import Model.Books;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class BookForm extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private ArrayList<Books> books;

    public BookForm() {
        setLayout(new BorderLayout());

        books = TransactionMockData.getBooks();

        String[] cols = {"Call Number", "ISBN", "Title", "Author", "Available"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        refreshTable();

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void refreshTable() {
        model.setRowCount(0);
        for (Books b : books) {
            model.addRow(new Object[]{
                    b.getCallNumber(), b.getISBN(), b.getTitle(),
                    b.getAuthor(), b.isAvailable() ? "Yes" : "No"
            });
        }
    }

    public void searchBook(String callNumber) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equalsIgnoreCase(callNumber)) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 0, true));
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "No book found with CallNumber: " + callNumber);
    }
}
