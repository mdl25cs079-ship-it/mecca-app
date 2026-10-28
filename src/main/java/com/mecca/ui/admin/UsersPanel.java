// src/main/java/com/mecca/ui/admin/UsersPanel.java
package com.mecca.ui.admin;

import com.mecca.model.User;
import com.mecca.service.AdminService;
import com.mecca.service.AuthService;
import com.mecca.util.Constants;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

/** Admin tab: add users, change roles, assign clubs to organizers, delete users. Owner: M3 (updated by M1). */
public class UsersPanel extends JPanel {

    /** Drop-down item that shows a club name but carries its id. */
    private static class ClubItem {
        final Integer id;   // null = no club
        final String name;
        ClubItem(Integer id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Name", "Email", "Role", "Roll No", "Club"}, 0) {
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
        JButton club = new JButton("Assign Club");
        JButton reset = new JButton("Reset Password");
        JButton del = new JButton("Delete");
        JButton refresh = new JButton("Refresh");
        add.addActionListener(e -> addUser());
        role.addActionListener(e -> changeRole());
        club.addActionListener(e -> assignClub());
        reset.addActionListener(e -> resetPassword());
        del.addActionListener(e -> deleteUser());
        refresh.addActionListener(e -> load());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(add);
        buttons.add(role);
        buttons.add(club);
        buttons.add(reset);
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

    /** Club drop-down: "(no club)" plus every club. */
    private JComboBox<ClubItem> clubCombo() {
        JComboBox<ClubItem> box = new JComboBox<>();
        box.addItem(new ClubItem(null, "(no club)"));
        try {
            for (Object[] c : AdminService.listClubChoices()) {
                box.addItem(new ClubItem((Integer) c[0], (String) c[1]));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load clubs: " + ex.getMessage());
        }
        return box;
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
        JComboBox<ClubItem> club = clubCombo();

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Name:"));               form.add(name);
        form.add(new JLabel("Email:"));              form.add(email);
        form.add(new JLabel("Password (6+):"));      form.add(pass);
        form.add(new JLabel("Role:"));               form.add(role);
        form.add(new JLabel("Roll no (students):")); form.add(roll);
        form.add(new JLabel("Admission type:"));     form.add(adm);
        form.add(new JLabel("Club (organizers):"));  form.add(club);

        int ok = JOptionPane.showConfirmDialog(this, form, "Add user",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;
        try {
            ClubItem chosen = (ClubItem) club.getSelectedItem();
            String error = AdminService.addUser(name.getText(), email.getText(),
                    new String(pass.getPassword()), (String) role.getSelectedItem(),
                    roll.getText(), (String) adm.getSelectedItem(),
                    chosen == null ? null : chosen.id);
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

    private void assignClub() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an organizer first.");
            return;
        }
        int id = (Integer) model.getValueAt(row, 0);
        String role = String.valueOf(model.getValueAt(row, 3));
        if (!Constants.ORGANIZER.equals(role)) {
            JOptionPane.showMessageDialog(this, "Only organizers can manage a club. Change the role first.");
            return;
        }
        JComboBox<ClubItem> club = clubCombo();
        int ok = JOptionPane.showConfirmDialog(this, club, "Assign club",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;
        ClubItem chosen = (ClubItem) club.getSelectedItem();
        if (!AdminService.assignClub(id, chosen == null ? null : chosen.id)) {
            JOptionPane.showMessageDialog(this, "Could not assign the club.");
        }
        load();
    }

    private void resetPassword() {
        Integer id = selectedId();
        if (id == null) return;
        JPasswordField first = new JPasswordField();
        JPasswordField second = new JPasswordField();
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("New password (6+):")); form.add(first);
        form.add(new JLabel("Confirm password:"));  form.add(second);
        int ok = JOptionPane.showConfirmDialog(this, form, "Reset password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;
        String a = new String(first.getPassword());
        String b = new String(second.getPassword());
        if (!a.equals(b)) {
            JOptionPane.showMessageDialog(this, "The two passwords do not match.");
            return;
        }
        String error = AdminService.resetPassword(id, a);
        JOptionPane.showMessageDialog(this, error == null ? "Password changed." : error);
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