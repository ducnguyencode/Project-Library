// UI/CopyPicker.java – chọn 1 copy nhanh khi đổi status
package UI;

import Model.BookCopy;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CopyPicker {
    public static BookCopy pick(Component parent, List<BookCopy> copies) {
        DefaultListModel<String> m = new DefaultListModel<>();
        for (BookCopy c : copies)
            m.addElement(c.copyId+" | "+c.callNumber+" | "+c.status+(c.isAvailable?" | On shelf":""));
        JList<String> list = new JList<>(m);
        int ok = JOptionPane.showConfirmDialog(parent,new JScrollPane(list),"Select Copy",JOptionPane.OK_CANCEL_OPTION);
        if(ok!=JOptionPane.OK_OPTION||list.getSelectedIndex()<0) return null;
        return copies.get(list.getSelectedIndex());
    }
}
