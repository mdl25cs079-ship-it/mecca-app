
package com.mecca.ui.admin;

import com.mecca.service.AnalyticsService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.Map;

/** Admin tab: system statistics and club engagement chart. Owner: M3 (updated by M1). */
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

    /** Reloads the numbers and the chart. A problem with one never hides the other. */
    private void load() {
        try {
            Map<String, Integer> s = AnalyticsService.getSystemStats();
            stats.setText("Users: " + s.get("users") + "   Clubs: " + s.get("clubs")
                    + "   Events: " + s.get("events") + "   Registrations: " + s.get("registrations")
                    + "   Attendance: " + s.get("attendance")
                    + " (" + s.get("attendanceRate") + "%)");
        } catch (Exception ex) {
            stats.setText("Could not load statistics: " + ex.getMessage());
        }
        try {
            showScores(AnalyticsService.getAllClubScores());
        } catch (Exception ex) {
            showMessage("Could not load the club chart: " + ex.getMessage());
        }
    }

    private void showMessage(String text) {
        chartHolder.removeAll();
        chartHolder.add(new JLabel(text, SwingConstants.CENTER), BorderLayout.CENTER);
        chartHolder.revalidate();
        chartHolder.repaint();
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

                // flat blue bars that stay slim, even with a single club
                BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
                renderer.setBarPainter(new StandardBarPainter());
                renderer.setSeriesPaint(0, new Color(0x2563EB));
                renderer.setMaximumBarWidth(0.15);

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