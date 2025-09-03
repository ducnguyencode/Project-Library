package UI;

import Model.Books;
import Model.Transaction;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class CheckInForm extends JPanel {
    private JTextField txtCallNumber;
    private JButton btnCheckIn;
    private JTextArea txtResult;

    private ArrayList<Books> books;
    private ArrayList<Transaction> transactions;

    public CheckInForm() {
        setLayout(new BorderLayout());

        books = TransactionMockData.getBooks();
        transactions = new ArrayList<>();

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
        form.add(new JLabel("Book CallNumber:"));
        txtCallNumber = new JTextField();
        form.add(txtCallNumber);

        btnCheckIn = new JButton("Check In");
        btnCheckIn.addActionListener(e -> doCheckIn());
        form.add(new JLabel());
        form.add(btnCheckIn);

        txtResult = new JTextArea(5, 30);
        txtResult.setEditable(false);

        add(form, BorderLayout.NORTH);
        add(new JScrollPane(txtResult), BorderLayout.CENTER);
    }

    private void doCheckIn() {
        String callNo = txtCallNumber.getText().trim();

        for (Transaction t : transactions) {
            if (t.getBook().getCallNumber().equals(callNo) && t.getReturnDate() == null) {
                t.returnBook(LocalDate.now());
                t.getBook().setAvailable(true);

                txtResult.append("Book Checked In!\n" + t + "\n\n");
                return;
            }
        }

        JOptionPane.showMessageDialog(this, "No active transaction for this book!");
    }
}
