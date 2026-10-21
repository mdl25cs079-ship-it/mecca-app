// com/mecca/ui/admin/ClaimsPanel.java
package com.mecca.ui.admin;

import com.mecca.model.ActivityPoint;
import com.mecca.service.ActivityPointsService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Admin panel for reviewing pending KTU activity point claims. */
public class ClaimsPanel extends JPanel {
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Student", "KTU Code", "Level", "Note"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable claimsTable = new JTable(tableModel);
    private final List<Integer> claimIds = new ArrayList<>();

    public ClaimsPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel heading = new JLabel("Pending KTU Points Claims");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 20f));
        add(heading, BorderLayout.NORTH);

        claimsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        claimsTable.setFillsViewportHeight(true);
        claimsTable.setRowHeight(24);
        add(new JScrollPane(claimsTable), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        JButton approveButton = new JButton("Approve");
        JButton rejectButton = new JButton("Reject");

        refreshButton.addActionListener(event -> loadClaims());
        approveButton.addActionListener(event -> approveSelectedClaim());
        rejectButton.addActionListener(event -> rejectSelectedClaim());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(refreshButton);
        actions.add(approveButton);
        actions.add(rejectButton);
        add(actions, BorderLayout.SOUTH);

        loadClaims();
    }

    private void loadClaims() {
        tableModel.setRowCount(0);
        claimIds.clear();

        try {
            List<ActivityPoint> claims = ActivityPointsService.getPendingClaims();
            if (claims == null) {
                return;
            }

            for (ActivityPoint claim : claims) {
                if (claim == null) {
                    continue;
                }

                claimIds.add(claim.getApId());
                tableModel.addRow(new Object[]{
                        safeText(claim.getStudentName()),
                        safeText(claim.getKtuCode()),
                        claim.getLevel(),
                        safeText(claim.getCertificateNote())
                });
            }
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load pending claims. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void approveSelectedClaim() {
        Integer apId = getSelectedClaimId();
        if (apId == null) {
            showSelectClaimMessage();
            return;
        }

        try {
            ActivityPointsService.approveClaim(apId);
            JOptionPane.showMessageDialog(
                    this,
                    "Claim approved.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );
            loadClaims();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not approve the claim. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void rejectSelectedClaim() {
        Integer apId = getSelectedClaimId();
        if (apId == null) {
            showSelectClaimMessage();
            return;
        }

        try {
            ActivityPointsService.rejectClaim(apId);
            JOptionPane.showMessageDialog(
                    this,
                    "Claim rejected.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );
            loadClaims();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not reject the claim. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private Integer getSelectedClaimId() {
        int selectedRow = claimsTable.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }

        int modelRow = claimsTable.convertRowIndexToModel(selectedRow);
        if (modelRow < 0 || modelRow >= claimIds.size()) {
            return null;
        }

        return claimIds.get(modelRow);
    }

    private void showSelectClaimMessage() {
        JOptionPane.showMessageDialog(
                this,
                "Select a claim first.",
                "No claim selected",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}