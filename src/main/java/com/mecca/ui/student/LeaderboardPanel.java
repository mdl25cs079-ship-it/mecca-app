// src/main/java/com/mecca/ui/student/LeaderboardPanel.java
package com.mecca.ui.student;

import com.mecca.model.LeaderboardRow;
import com.mecca.model.User;
import com.mecca.service.AnalyticsService;
import com.mecca.service.AuthService;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Ranked table of students by approved points; the logged-in user is highlighted. Owner: M3. */
public class LeaderboardPanel extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Rank", "Name", "Roll No", "Total Points"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JLabel message = new JLabel(" ");
    // user id for each table row, used for highlighting
    private final List<Integer> userIds = new ArrayList<>();
    private int currentUserId = -1;

    public LeaderboardPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        table.setRowHeight(24);
        table.setDefaultRenderer(Object.class, new HighlightRenderer());
        message.setFont(message.getFont().deriveFont(Font.ITALIC));

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> load());
        JPanel top = new JPanel(new BorderLayout());
        top.add(new JLabel("Top students by approved KTU points"), BorderLayout.WEST);
        top.add(refresh, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(message, BorderLayout.SOUTH);

        // reload each time the tab is shown
        addAncestorListener(new AncestorListener() {
            @Override public void ancestorAdded(AncestorEvent e) { load(); }
            @Override public void ancestorRemoved(AncestorEvent e) { }
            @Override public void ancestorMoved(AncestorEvent e) { }
        });
        load();
    }

    private void load() {
        User u = AuthService.getCurrentUser();
        currentUserId = (u == null) ? -1 : u.getUserId();
        model.setRowCount(0);
        userIds.clear();
        try {
            List<LeaderboardRow> rows = AnalyticsService.getLeaderboard(20);
            for (LeaderboardRow r : rows) {
                model.addRow(new Object[]{r.getRank(), r.getName(),
                        r.getRollNumber() == null ? "-" : r.getRollNumber(), r.getTotalPoints()});
                userIds.add(r.getUserId());
            }
            message.setText(rows.isEmpty() ? "No points yet. Attend an event to get on the board." : " ");
        } catch (Exception ex) {
            message.setText("Could not load leaderboard: " + ex.getMessage());
        }
    }

    /** Highlights the row that belongs to the logged-in user. */
    private class HighlightRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                       boolean focus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            boolean mine = row < userIds.size() && userIds.get(row).intValue() == currentUserId;
            if (!sel) {
                c.setBackground(mine ? new Color(200, 230, 201) : Color.WHITE);
                c.setForeground(Color.BLACK);
            }
            c.setFont(c.getFont().deriveFont(mine ? Font.BOLD : Font.PLAIN));
            return c;
        }
    }
}