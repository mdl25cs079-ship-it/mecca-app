// com/mecca/ui/student/PointsPanel.java
package com.mecca.ui.student;

import com.mecca.model.ActivityPoint;
import com.mecca.model.PointsSummary;
import com.mecca.model.User;
import com.mecca.service.ActivityPointsService;
import com.mecca.service.AuthService;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/** Displays a student's KTU activity point progress and history. */
public class PointsPanel extends JPanel {
    private static final boolean USE_FAKE_DATA = false;

    private final JProgressBar[] groupBars = new JProgressBar[3];
    private final JLabel[] groupLabels = new JLabel[3];
    private final JLabel[] remainingLabels = new JLabel[3];
    private final JProgressBar overallBar = new JProgressBar();
    private final JLabel overallLabel = new JLabel("Overall");
    private final JLabel statusLabel = new JLabel("In progress", SwingConstants.CENTER);
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Date", "Activity", "KTU Code", "Group", "Level", "Points", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public PointsPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildProgressPanel(), BorderLayout.NORTH);
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> refreshData());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(refreshButton);
        add(bottom, BorderLayout.SOUTH);

        addHierarchyListener(event -> {
            if ((event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                refreshData();
            }
        });

        refreshData();
    }

    private JPanel buildProgressPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("KTU Activity Points"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(7, 8, 7, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        String[] names = {"Group A", "Group B", "Group C"};
        for (int i = 0; i < 3; i++) {
            final int index = i;
            groupLabels[i] = new JLabel(names[i]);
            groupBars[i] = new JProgressBar();
            groupBars[i].setStringPainted(true);
            groupBars[i].setForeground(Color.RED);
            remainingLabels[i] = new JLabel("Remaining: —");

            c.gridx = 0;
            c.gridy = i;
            c.weightx = 0;
            panel.add(groupLabels[i], c);
            c.gridx = 1;
            c.weightx = 1;
            panel.add(groupBars[i], c);
            c.gridx = 2;
            c.weightx = 0;
            panel.add(remainingLabels[i], c);
        }

        overallBar.setStringPainted(true);
        overallBar.setForeground(new Color(55, 125, 190));
        c.gridx = 0;
        c.gridy = 3;
        panel.add(overallLabel, c);
        c.gridx = 1;
        c.gridwidth = 2;
        c.weightx = 1;
        panel.add(overallBar, c);

        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 18f));
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 3;
        panel.add(statusLabel, c);

        return panel;
    }

    private void refreshData() {
        try {
            PointsSummary summary;
            List<ActivityPoint> history;

            if (USE_FAKE_DATA) {
                summary = FakePointsData.summary();
                history = FakePointsData.history();
            } else {
                User user = AuthService.getCurrentUser();
                if (user == null) {
                    showEmptyState("Please sign in to view your points.");
                    return;
                }
                summary = ActivityPointsService.getSummary(user.getUserId());
                history = ActivityPointsService.getHistory(user.getUserId());
            }

            if (summary == null) {
                showEmptyState("Point summary is not available.");
                return;
            }

            int[] earned = summary.getEarned();
            int[] required = summary.getRequired();

            for (int i = 0; i < 3; i++) {
                int have = valueAt(earned, i);
                int need = valueAt(required, i);
                int capped = Math.min(Math.max(have, 0), Math.max(need, 0));

                groupBars[i].setMaximum(Math.max(need, 1));
                groupBars[i].setValue(capped);
                groupBars[i].setString(capped + "/" + need);

                Color color = capped == 0 ? new Color(190, 55, 55)
                        : (need > 0 && capped >= need ? new Color(55, 145, 80)
                        : new Color(225, 145, 35));
                groupBars[i].setForeground(color);
                remainingLabels[i].setText("Remaining: " + Math.max(0, need - capped));
            }

            int totalRequired = Math.max(0, summary.getTotalRequired());
            int total = Math.max(0, Math.min(summary.getTotal(), totalRequired));
            overallBar.setMaximum(Math.max(1, totalRequired));
            overallBar.setValue(total);
            overallBar.setString(total + "/" + totalRequired);
            overallLabel.setText("Overall");

            statusLabel.setText(summary.isCreditsComplete() ? "Credits complete" : "In progress");
            statusLabel.setForeground(summary.isCreditsComplete()
                    ? new Color(45, 130, 70) : new Color(70, 70, 70));

            populateHistory(history);
        } catch (Exception exception) {
            showEmptyState("Could not load points. Please try refreshing.");
        }
    }

    private void populateHistory(List<ActivityPoint> history) {
        tableModel.setRowCount(0);
        if (history == null) {
            return;
        }

        for (ActivityPoint point : history) {
            if (point == null) {
                continue;
            }
            tableModel.addRow(new Object[]{
                    point.getAwardedOn(),
                    point.getActivityName(),
                    point.getKtuCode(),
                    groupName(point.getGroupNo()),
                    point.getLevel(),
                    point.getPoints(),
                    point.getStatus()
            });
        }
    }

    private void showEmptyState(String message) {
        tableModel.setRowCount(0);
        statusLabel.setText(message);
        statusLabel.setForeground(new Color(110, 75, 30));
        for (int i = 0; i < 3; i++) {
            groupBars[i].setValue(0);
            groupBars[i].setString("—");
            remainingLabels[i].setText("Remaining: —");
        }
        overallBar.setValue(0);
        overallBar.setString("—");
    }

    private int valueAt(int[] values, int index) {
        return values != null && index < values.length ? values[index] : 0;
    }

    private String groupName(int groupNo) {
        return groupNo >= 1 && groupNo <= 3 ? "ABC".substring(groupNo - 1, groupNo) : "—";
    }

    /**
     * Local sample data for UI checks before the points backend is available.
     * Enable it by setting USE_FAKE_DATA to true.
     */
    private static final class FakePointsData {
        private static PointsSummary summary() {
            PointsSummary result = new PointsSummary();
            result.setAdmissionType("REGULAR");
            result.setEarned(new int[]{24, 10, 0});
            result.setRequired(new int[]{40, 40, 40});
            result.setTotal(34);
            result.setTotalRequired(120);
            result.setCreditsComplete(false);
            return result;
        }

        private static List<ActivityPoint> history() {
            return Collections.emptyList();
        }
        
    }
    
}