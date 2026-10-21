// src/main/java/com/mecca/ui/admin/AnalyticsPanel.java
package com.mecca.ui.admin;

import com.mecca.service.AnalyticsService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

/** Admin tab: system statistics and club engagement chart. Owner: M3. */
public class AnalyticsPanel extends JPanel {
    private final JLabel stats = new JLabel(" ");
    private final JPanel chartHolder = new JPanel(new BorderLayout());

    public AnalyticsPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        stats.setFont(stats.getFont().deriveFont(Font.BOLD, 14f));

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> load());
        JPanel top = new JPanel(new BorderLayout());
        top.add(stats, BorderLayout.CENTER);
        top.add(refresh, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(chartHolder, BorderLayout.CENTER);
        load();
    }

    private void load() {
        try {
            Map<String, Integer> s = AnalyticsService.getSystemStats();
            stats.setText("Users: " + s.get("users") + "   Clubs: " + s.get("clubs")
                    + "   Events: " + s.get("events") + "   Registrations: " + s.get("registrations")
                    + "   Attendance: " + s.get("attendance")
                    + " (" + s.get("attendanceRate") + "%)");
            showScores(AnalyticsService.getAllClubScores());
        } catch (Exception ex) {
            stats.setText("Could not load analytics: " + ex.getMessage());
        }
    }

    private void showScores(Map<String, Double> scores) {
        chartHolder.removeAll();
        if (scores.isEmpty()) {
            chartHolder.add(new JLabel("No clubs yet.", SwingConstants.CENTER), BorderLayout.CENTER);
        } else {
            try {
                DefaultCategoryDataset ds = new DefaultCategoryDataset();
                scores.forEach((club, score) -> ds.addValue(score, "Engagement %", club));
                JFreeChart chart = ChartFactory.createBarChart("Club engagement score",
                        "Club", "Score (%)", ds, PlotOrientation.VERTICAL, false, true, false);
                chartHolder.add(new ChartPanel(chart), BorderLayout.CENTER);
            } catch (Throwable t) {
                // fallback: plain table if the chart library fails
                DefaultTableModel m = new DefaultTableModel(new String[]{"Club", "Engagement %"}, 0);
                scores.forEach((club, score) -> m.addRow(new Object[]{club, score}));
                chartHolder.add(new JScrollPane(new JTable(m)), BorderLayout.CENTER);
            }
        }
        chartHolder.revalidate();
        chartHolder.repaint();
    }
}
