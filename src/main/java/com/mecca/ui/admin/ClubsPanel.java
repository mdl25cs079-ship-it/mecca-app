// src/main/java/com/mecca/ui/admin/ClubsPanel.java
package com.mecca.ui.admin;

import com.mecca.service.AdminService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

/** Admin tab: create, approve or reject clubs. Owner: M3 (updated by M1). */
public class ClubsPanel extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Name", "Description", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public ClubsPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton addClub = new JButton("Add Club");
        JButton approve = new JButton("Approve");
        JButton reject = new JButton("Reject");
        JButton refresh = new JButton("Refresh");
        addClub.addActionListener(e -> addClub());
        approve.addActionListener(e -> approveSelected());
        reject.addActionListener(e -> rejectSelected());
        refresh.addActionListener(e -> load());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(addClub);
        buttons.add(approve);
        buttons.add(reject);
        buttons.add(refresh);
        add(buttons, BorderLayout.SOUTH);
        load();
    }

    private void load() {
        try {
            List<Object[]> rows = AdminService.listClubs();
            model.setRowCount(0);
            for (Object[] r : rows) {
                model.addRow(r);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load clubs: " + ex.getMessage());
        }
    }

    private Integer selectedId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a club first.");
            return null;
        }
        return (Integer) model.getValueAt(row, 0);
    }

    private void addClub() {
        JTextField name = new JTextField();
        JTextField description = new JTextField();
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Club name:"));   form.add(name);
        form.add(new JLabel("Description:")); form.add(description);
        int ok = JOptionPane.showConfirmDialog(this, form, "Add club",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;
        String error = AdminService.addClub(name.getText(), description.getText());
        if (error != null) {
            JOptionPane.showMessageDialog(this, error);
        }
        load();
    }

    private void approveSelected() {
        Integer id = selectedId();
        if (id == null) return;
        if (!AdminService.approveClub(id)) {
            JOptionPane.showMessageDialog(this, "Could not approve this club.");
        }
        load();
    }

    private void rejectSelected() {
        Integer id = selectedId();
        if (id == null) return;
        int ok = JOptionPane.showConfirmDialog(this,
                "Rejecting removes the club. Continue?", "Reject club", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        if (!AdminService.rejectClub(id)) {
            JOptionPane.showMessageDialog(this,
                    "Cannot reject: this club already has events or organizers.");
        }
        load();
    }
}