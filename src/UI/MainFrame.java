package UI;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JPanel sidebar;
    private JPanel content;

    public MainFrame() {
        setTitle("Library Manager System");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Sidebar
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(200, getHeight()));

        JButton btnBooks = new JButton("Books");
        JButton btnEmployees = new JButton("Employees");
        JButton btnCheckIn = new JButton("Check In");
        JButton btnCheckOut = new JButton("Check Out");
        JButton btnLogout = new JButton("Logout");

        JTextField txtSearch = new JTextField();
        txtSearch.setMaximumSize(new Dimension(180, 30));
        JButton btnSearch = new JButton("Search");

        btnBooks.addActionListener(e -> setContent(new BookForm()));
        btnEmployees.addActionListener(e -> setContent(new EmployeeForm()));
        btnCheckIn.addActionListener(e -> setContent(new CheckInForm()));
        btnCheckOut.addActionListener(e -> setContent(new CheckOutForm()));
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        btnSearch.addActionListener(e -> {
            if (content instanceof BookForm bookForm) {
                bookForm.searchBook(txtSearch.getText().trim());
            }
        });
        
        JButton btnTransactions = new JButton("Transactions");
        btnTransactions.addActionListener(e -> setContent(new TransactionForm()));
        
        sidebar.add(btnBooks);
        sidebar.add(btnEmployees);
        sidebar.add(btnCheckIn);
        sidebar.add(btnCheckOut);
        sidebar.add(btnTransactions);
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(new JLabel("Search by CallNumber:"));
        sidebar.add(txtSearch);
        sidebar.add(btnSearch);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(btnLogout);

        // Content panel
        content = new JPanel(new BorderLayout());
        setContent(new BookForm());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(content, BorderLayout.CENTER);
    }

    private void setContent(JPanel panel) {
        content.removeAll();
        content.add(panel, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }
}
