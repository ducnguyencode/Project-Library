package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class MainFrame extends JFrame {
    private JPanel sidebar;
    private JPanel content;
    private JTextField txtSearch;

    public MainFrame() {
        setTitle("Library Manager System");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ==== MENU BAR ====
        setJMenuBar(buildMenuBar());

        // ==== Sidebar ====
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(160, getHeight()));

        JButton btnBooks      = new JButton("Books");
        JButton btnEmployees  = new JButton("Employees");
        JButton btnCheckIn    = new JButton("Check In");
        JButton btnCheckOut   = new JButton("Check Out");
        JButton btnTrans      = new JButton("Transactions");
        JButton btnLogout     = new JButton("Logout");

        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(btnBooks);
        sidebar.add(btnEmployees);
        sidebar.add(btnCheckIn);
        sidebar.add(btnCheckOut);
        sidebar.add(btnTrans);

        sidebar.add(Box.createVerticalStrut(16));
        sidebar.add(new JLabel("Search by CallNumber:"));
        txtSearch = new JTextField();
        txtSearch.setMaximumSize(new Dimension(140, 28));
        JButton btnSearch = new JButton("Search");
        sidebar.add(txtSearch);
        sidebar.add(btnSearch);

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(btnLogout);

        // ==== Content ====
        content = new JPanel(new BorderLayout());
        setContent(new BookForm()); // mặc định mở Books

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(content, BorderLayout.CENTER);

        // ==== Actions ====
        btnBooks.addActionListener(e -> setContent(new BookForm()));
        btnEmployees.addActionListener(e -> setContent(new EmployeeForm()));
        btnCheckIn.addActionListener(e -> setContent(new CheckInForm()));
        btnCheckOut.addActionListener(e -> setContent(new CheckOutForm()));
        btnTrans.addActionListener(e -> setContent(new TransactionForm()));
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        btnSearch.addActionListener(e -> {
            BookForm bf = getActiveBookForm();
            if (bf != null) bf.searchBook(txtSearch.getText().trim());
        });
    }

    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();
        int M = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        // File
        JMenu mFile = new JMenu("File");
        JMenuItem miLogout = new JMenuItem("Logout");
        JMenuItem miExit   = new JMenuItem("Exit");
        miLogout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });
        miExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, M));
        miExit.addActionListener(e -> System.exit(0));
        mFile.add(miLogout);
        mFile.addSeparator();
        mFile.add(miExit);

        // Books
        JMenu mBooks = new JMenu("Books");
        JMenuItem miBooks = new JMenuItem("Manage Books");
        miBooks.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, M));
        miBooks.addActionListener(e -> setContent(new BookForm()));
        JMenuItem miSearchCall = new JMenuItem("Search by CallNumber");
        miSearchCall.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, M));
        miSearchCall.addActionListener(e -> {
            BookForm bf = getActiveBookForm();
            if (bf != null) bf.searchBook(txtSearch.getText().trim());
        });
        mBooks.add(miBooks);
        mBooks.add(miSearchCall);

        // Transactions
        JMenu mTrans = new JMenu("Transactions");
        JMenuItem miCheckIn  = new JMenuItem("Check In");
        JMenuItem miCheckOut = new JMenuItem("Check Out");
        miCheckIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, M));
        miCheckOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, M));
        miCheckIn.addActionListener(e -> setContent(new CheckInForm()));
        miCheckOut.addActionListener(e -> setContent(new CheckOutForm()));
        mTrans.add(miCheckIn);
        mTrans.add(miCheckOut);

        // Admin
        JMenu mAdmin = new JMenu("Admin");
        JMenuItem miEmployees = new JMenuItem("Employees");
        miEmployees.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, M));
        miEmployees.addActionListener(e -> setContent(new EmployeeForm()));
        mAdmin.add(miEmployees);

        // Help
        JMenu mHelp = new JMenu("Help");
        JMenuItem miAbout = new JMenuItem("About");
        miAbout.addActionListener(e ->
            JOptionPane.showMessageDialog(this,
                "Library Manager System\n© Your Team", "About",
                JOptionPane.INFORMATION_MESSAGE));
        mHelp.add(miAbout);

        mb.add(mFile);
        mb.add(mBooks);
        mb.add(mTrans);
        mb.add(mAdmin);
        mb.add(mHelp);
        return mb;
    }

    private void setContent(JPanel panel) {
        content.removeAll();
        content.add(panel, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    private BookForm getActiveBookForm() {
        if (content.getComponentCount() == 0) return null;
        Component c = content.getComponent(0);
        return (c instanceof BookForm) ? (BookForm) c : null;
    }
}
