package UI;

import Model.Books;
import Model.Employee;
import Model.Transaction;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

public class CheckOutForm extends JPanel {
    private JTextField txtEmployeeId, txtCallNumber;
    private JButton btnCheckOut;
    private JTextArea txtResult;

    // Mock data lists 
    private ArrayList<Books> books;
    private ArrayList<Employee> employees;
    private ArrayList<Transaction> transactions;

    public CheckOutForm() {
        setLayout(new BorderLayout());

        books = TransactionMockData.getBooks();
        employees = TransactionMockData.getEmployees();
        transactions = TransactionMockData.getTransactions();

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.add(new JLabel("Employee ID:"));
        txtEmployeeId = new JTextField();
        form.add(txtEmployeeId);

        form.add(new JLabel("Book CallNumber:"));
        txtCallNumber = new JTextField();
        form.add(txtCallNumber);

        btnCheckOut = new JButton("Check Out");
        btnCheckOut.addActionListener(e -> doCheckOut());
        form.add(new JLabel());
        form.add(btnCheckOut);

        txtResult = new JTextArea(5, 30);
        txtResult.setEditable(false);

        add(form, BorderLayout.NORTH);
        add(new JScrollPane(txtResult), BorderLayout.CENTER);
    }

    private void doCheckOut() {
        String empId = txtEmployeeId.getText().trim();
        String callNo = txtCallNumber.getText().trim();

        Employee emp = employees.stream().filter(e -> e.getEmployeeID().equals(empId)).findFirst().orElse(null);
        if (emp == null) {
            JOptionPane.showMessageDialog(this, "Employee not found!");
            return;
        }

        Books book = books.stream().filter(b -> b.getCallNumber().equals(callNo)).findFirst().orElse(null);
        if (book == null || !book.isAvailable()) {
            JOptionPane.showMessageDialog(this, "Book not available!");
            return;
        }

        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(5);

        Transaction t = new Transaction(UUID.randomUUID().toString(), emp, book, issueDate, dueDate);
        transactions.add(t);
        book.setAvailable(false);

        txtResult.append("Book Checked Out!\n" + t + "\n\n");
    }
}
