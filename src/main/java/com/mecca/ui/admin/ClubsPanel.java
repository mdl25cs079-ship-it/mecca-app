// src/main/java/com/mecca/ui/admin/ClubsPanel.java
package com.mecca.ui.admin;

import com.mecca.service.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Admin tab: approve or reject clubs. Owner: M3. */
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

        JButton approve = new JButton("Approve");
        JButton reject = new JButton("Reject");
        JButton refresh = new JButton("Refresh");
        approve.addActionListener(e -> approveSelected());
        reject.addActionListener(e -> rejectSelected());
        refresh.addActionListener(e -> load());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
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