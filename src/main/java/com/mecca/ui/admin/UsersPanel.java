// src/main/java/com/mecca/ui/admin/UsersPanel.java
package com.mecca.ui.admin;

import com.mecca.model.User;
import com.mecca.service.AdminService;
import com.mecca.service.AuthService;
import com.mecca.util.Constants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Admin tab: add users, change roles, delete users. Owner: M3. */
public class UsersPanel extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Name", "Email", "Role", "Roll No"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public UsersPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton add = new JButton("Add User");
        JButton role = new JButton("Change Role");
        JButton del = new JButton("Delete");
        JButton refresh = new JButton("Refresh");
        add.addActionListener(e -> addUser());
        role.addActionListener(e -> changeRole());
        del.addActionListener(e -> deleteUser());
        refresh.addActionListener(e -> load());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(add);
        buttons.add(role);
        buttons.add(del);
        buttons.add(refresh);
        add(buttons, BorderLayout.SOUTH);
        load();
    }

    private void load() {
        try {
            List<Object[]> rows = AdminService.listUsers();
            model.setRowCount(0);
            for (Object[] r : rows) {
                model.addRow(r);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load users: " + ex.getMessage());
        }
    }

    private Integer selectedId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user first.");
            return null;
        }
        return (Integer) model.getValueAt(row, 0);
    }

    private void addUser() {
        JTextField name = new JTextField();
        JTextField email = new JTextField();
        JPasswordField pass = new JPasswordField();
        JComboBox<String> role = new JComboBox<>(
                new String[]{Constants.STUDENT, Constants.ORGANIZER, Constants.ADMIN});
        JTextField roll = new JTextField();
        JComboBox<String> adm = new JComboBox<>(
                new String[]{Constants.ADM_REGULAR, Constants.ADM_LATERAL, Constants.ADM_PWD});

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Name:"));            form.add(name);
        form.add(new JLabel("Email:"));           form.add(email);
        form.add(new JLabel("Password:"));        form.add(pass);
        form.add(new JLabel("Role:"));            form.add(role);
        form.add(new JLabel("Roll no (students):")); form.add(roll);
        form.add(new JLabel("Admission type:"));  form.add(adm);

        int ok = JOptionPane.showConfirmDialog(this, form, "Add user",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;
        try {
            String error = AdminService.addUser(name.getText(), email.getText(),
                    new String(pass.getPassword()), (String) role.getSelectedItem(),
                    roll.getText(), (String) adm.getSelectedItem());
            if (error != null) {
                JOptionPane.showMessageDialog(this, error);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not add user: " + ex.getMessage());
        }
        load();
    }

    private void changeRole() {
        Integer id = selectedId();
        if (id == null) return;
        if (isCurrentUser(id)) {
            JOptionPane.showMessageDialog(this, "You cannot change your own role.");
            return;
        }
        String[] roles = {Constants.STUDENT, Constants.ORGANIZER, Constants.ADMIN};
        Object pick = JOptionPane.showInputDialog(this, "New role:", "Change role",
                JOptionPane.PLAIN_MESSAGE, null, roles, roles[0]);
        if (pick == null) return;
        if (!AdminService.changeRole(id, (String) pick)) {
            JOptionPane.showMessageDialog(this, "Could not change the role.");
        }
        load();
    }

    private void deleteUser() {
        Integer id = selectedId();
        if (id == null) return;
        if (isCurrentUser(id)) {
            JOptionPane.showMessageDialog(this, "You cannot delete the account you are using.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Delete this user?", "Delete user",
                JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        if (!AdminService.deleteUser(id)) {
            JOptionPane.showMessageDialog(this, "Cannot delete: this user has registrations or points.");
        }
        load();
    }

    private boolean isCurrentUser(int id) {
        User u = AuthService.getCurrentUser();
        return u != null && u.getUserId() == id;
    }
}