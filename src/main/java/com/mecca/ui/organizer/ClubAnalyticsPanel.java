// src/main/java/com/mecca/ui/organizer/ClubAnalyticsPanel.java
package com.mecca.ui.organizer;

import com.mecca.db.DatabaseConnection;
import com.mecca.model.Organizer;
import com.mecca.model.User;
import com.mecca.service.AnalyticsService;
import com.mecca.service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Organizer tab: engagement numbers for the organizer's own club. Owner: M1. */
public class ClubAnalyticsPanel extends JPanel {

    private final JLabel summary = new JLabel(" ");
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Event", "Date", "Status", "Registered", "Attended", "Attendance %", "Avg Rating"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    public ClubAnalyticsPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        summary.setFont(summary.getFont().deriveFont(Font.BOLD, 14f));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refresh());

        JPanel top = new JPanel(new BorderLayout());
        top.add(summary, BorderLayout.CENTER);
        top.add(refreshButton, BorderLayout.EAST);

        JTable table = new JTable(model);
        table.setRowHeight(24);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    /** Reloads the numbers (also called every time the tab is opened). */
    public void refresh() {
        model.setRowCount(0);
        User user = AuthService.getCurrentUser();
        int clubId = (user instanceof Organizer) ? ((Organizer) user).getManagedClubId() : 0;
        if (clubId <= 0) {
            summary.setText("Your account is not linked to a club yet. Ask the admin to assign one.");
            return;
        }
        String sql = "SELECT e.title, e.date_time, e.status, "
                + "(SELECT COUNT(*) FROM registrations r WHERE r.event_id = e.event_id AND r.status = 'CONFIRMED') AS regs, "
                + "(SELECT COUNT(*) FROM attendance a JOIN registrations r ON r.reg_id = a.reg_id "
                + "   WHERE r.event_id = e.event_id) AS atts, "
                + "(SELECT ROUND(AVG(f.rating), 1) FROM feedback f WHERE f.event_id = e.event_id) AS avg_rating "
                + "FROM events e WHERE e.club_id = ? ORDER BY e.date_time DESC";
        int events = 0;
        int regs = 0;
        int atts = 0;
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int r = rs.getInt("regs");
                    int a = rs.getInt("atts");
                    Object rating = rs.getObject("avg_rating");
                    model.addRow(new Object[]{rs.getString("title"), rs.getString("date_time"),
                            rs.getString("status"), r, a,
                            r == 0 ? "-" : Math.round(a * 100.0 / r) + "%",
                            rating == null ? "-" : rating});
                    events++;
                    regs += r;
                    atts += a;
                }
            }
            double score = AnalyticsService.computeEngagementScore(clubId);
            summary.setText("Events: " + events + "     Registrations: " + regs
                    + "     Attended: " + atts + "     Engagement score: " + score + "%");
        } catch (SQLException | RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Could not load club analytics: " + ex.getMessage(),
                    "Club Analytics", JOptionPane.ERROR_MESSAGE);
        }
    }
}