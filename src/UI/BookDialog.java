package UI;

import DAO.BookDAO;
import DAO.SubjectDAO;
import Model.Books;
import Model.Subject;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.math.BigDecimal;
import java.util.List;

public final class BookDialog extends JDialog {
    private final JTextField tfTitle  = new JTextField(28);
    private final JTextField tfAuthor = new JTextField(24);
    private final JComboBox<Subject> cbSubject = new JComboBox<>();
    private final JTextField tfPrice  = new JTextField(10);
    private final JTextField tfImage  = new JTextField(28);
    private final JTextArea  taDesc   = new JTextArea(4, 28);

    private final SubjectDAO subjectDAORef;
    private Books model; // dùng khi edit

    /** Nạp lại list subject và optionally chọn subjectCode */
    private void reloadSubjects(Integer selectCode) throws Exception {
        List<Subject> subjects = subjectDAORef.findAll();
        DefaultComboBoxModel<Subject> m = new DefaultComboBoxModel<>();
        for (Subject s : subjects) m.addElement(s);
        cbSubject.setModel(m);
        if (selectCode != null) {
            for (int i = 0; i < m.getSize(); i++) {
                if (m.getElementAt(i).subjectCode == selectCode) {
                    cbSubject.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private BookDialog(Window owner, Long bookId, SubjectDAO subjectDAO) throws Exception {
        super(owner, "Book", ModalityType.APPLICATION_MODAL);
        this.subjectDAORef = subjectDAO;

        // panel Subject + nút New
        JPanel subjectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        subjectPanel.add(cbSubject);
        JButton btnNewSubject = new JButton("New…");
        subjectPanel.add(Box.createHorizontalStrut(6));
        subjectPanel.add(btnNewSubject);

        // Hàng Image URL với Paste + Browse
        JPanel imageRow = new JPanel(new BorderLayout(6, 0));
        JButton btnPaste  = new JButton("Paste");
        JButton btnBrowse = new JButton("Browse…");
        JPanel rightBtns  = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        rightBtns.add(btnPaste);
        rightBtns.add(btnBrowse);
        imageRow.add(tfImage, BorderLayout.CENTER);
        imageRow.add(rightBtns, BorderLayout.EAST);

        btnPaste.addActionListener(e -> {
            try {
                var cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                Object data = cb.getData(DataFlavor.stringFlavor);
                if (data != null) tfImage.setText(data.toString().trim());
            } catch (Exception ignore) {}
        });
        btnBrowse.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Images", "png","jpg","jpeg","gif","bmp","webp"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                tfImage.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });

        // Nạp danh sách Subject lần đầu
        reloadSubjects(null);

        // Nếu edit -> load book
        if (bookId != null) {
            Books b = new BookDAO().getById(bookId);
            model = b;
            tfTitle.setText(b.title);
            tfAuthor.setText(b.author);
            tfPrice.setText(b.price == null ? "" : b.price.toPlainString());
            tfImage.setText(b.imageURL == null ? "" : b.imageURL);
            taDesc.setText(b.description == null ? "" : b.description);
            reloadSubjects(b.subjectCode);
        }

        // Layout
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0; g.gridy = 0; form.add(new JLabel("Title:"), g);
        g.gridx = 1;              form.add(tfTitle, g);

        g.gridx = 0; g.gridy++;   form.add(new JLabel("Author:"), g);
        g.gridx = 1;              form.add(tfAuthor, g);

        g.gridx = 0; g.gridy++;   form.add(new JLabel("Subject:"), g);
        g.gridx = 1;              form.add(subjectPanel, g);

        g.gridx = 0; g.gridy++;   form.add(new JLabel("Price:"), g);
        g.gridx = 1;              form.add(tfPrice, g);

        g.gridx = 0; g.gridy++;   form.add(new JLabel("Image URL:"), g);
        g.gridx = 1;              form.add(imageRow, g);

        g.gridx = 0; g.gridy++;   form.add(new JLabel("Description:"), g);
        g.gridx = 1;              form.add(new JScrollPane(taDesc), g);

        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(ok);
        buttons.add(cancel);
        getRootPane().setDefaultButton(ok);

        // Thêm Subject nhanh
        btnNewSubject.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "New subject name:");
            if (name == null) return;
            name = name.trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Subject name required");
                return;
            }
            try {
                Subject s = subjectDAORef.insert(name);   // cần phương thức insert(name) trong SubjectDAO
                reloadSubjects(s.subjectCode);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        ok.addActionListener(e -> {
            if (tfTitle.getText().isBlank() || tfAuthor.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Title & Author are required");
                return;
            }
            Subject s = (Subject) cbSubject.getSelectedItem();
            if (s == null) {
                JOptionPane.showMessageDialog(this, "Please add/select a Subject first");
                return;
            }

            Books b = (model == null) ? new Books() : model;
            b.title = tfTitle.getText().trim();
            b.author = tfAuthor.getText().trim();
            b.subjectCode = s.subjectCode;
            b.description = taDesc.getText().trim();

            try {
                b.price = tfPrice.getText().isBlank()
                        ? BigDecimal.ZERO
                        : new BigDecimal(tfPrice.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid price");
                return;
            }
            b.imageURL = tfImage.getText().trim();

            model = b;
            dispose();
        });

        cancel.addActionListener(e -> { model = null; dispose(); });

        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    /** Mở dialog. Nếu bookId==null => thêm mới; ngược lại => chỉnh sửa. */
    public static Books show(Long bookId, SubjectDAO subjectDAO) {
        try {
            Window owner = KeyboardFocusManager
                    .getCurrentKeyboardFocusManager()
                    .getActiveWindow();
            BookDialog d = new BookDialog(owner, bookId, subjectDAO);
            d.setVisible(true);
            return d.model;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}
