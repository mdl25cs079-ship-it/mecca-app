// src/main/java/com/mecca/service/AnalyticsService.java
package com.mecca.service;

import com.mecca.db.DatabaseConnection;
import com.mecca.model.LeaderboardRow;
import com.mecca.util.Constants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Statistics, engagement scores and leaderboard. All methods are static. Owner: M3.
 *  Database errors are thrown as IllegalStateException; the panels catch them. */
public class AnalyticsService {

    private AnalyticsService() { }

    /** Keys: users, clubs, events, registrations, attendance, attendanceRate (0-100). */
    public static Map<String, Integer> getSystemStats() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        int registrations = count("SELECT COUNT(*) FROM registrations WHERE status = 'CONFIRMED'");
        int attendance = count("SELECT COUNT(*) FROM attendance");
        stats.put("users", count("SELECT COUNT(*) FROM users"));
        stats.put("clubs", count("SELECT COUNT(*) FROM clubs"));
        stats.put("events", count("SELECT COUNT(*) FROM events"));
        stats.put("registrations", registrations);
        stats.put("attendance", attendance);
        // Avoid divide by zero when nobody has registered yet
        stats.put("attendanceRate", registrations == 0 ? 0 : (int) Math.round(attendance * 100.0 / registrations));
        return stats;
    }

    /** Score = attendances / confirmed registrations * 100 over the club's events. 0 if none. Saved to clubs. */
    public static double computeEngagementScore(int clubId) {
        String sql = "SELECT COUNT(r.reg_id) AS regs, COUNT(a.att_id) AS atts "
                   + "FROM events e "
                   + "JOIN registrations r ON r.event_id = e.event_id AND r.status = 'CONFIRMED' "
                   + "LEFT JOIN attendance a ON a.reg_id = r.reg_id "
                   + "WHERE e.club_id = ?";
        double score = 0;
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt("regs") > 0) {
                    score = rs.getInt("atts") * 100.0 / rs.getInt("regs");
                    score = Math.round(score * 10.0) / 10.0;   // one decimal place
                }
            }
            try (PreparedStatement up = connection().prepareStatement(
                    "UPDATE clubs SET engagement_score = ? WHERE club_id = ?")) {
                up.setDouble(1, score);
                up.setInt(2, clubId);
                up.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not compute engagement score: " + e.getMessage(), e);
        }
        return score;
    }

    /** Club name -> engagement score for the chart. Recomputes every club first. */
    public static Map<String, Double> getAllClubScores() {
        List<Integer> ids = new ArrayList<>();
        List<String> names = new ArrayList<>();
        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT club_id, name FROM clubs ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getInt("club_id"));
                names.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load clubs: " + e.getMessage(), e);
        }
        Map<String, Double> scores = new LinkedHashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            scores.put(names.get(i), computeEngagementScore(ids.get(i)));
        }
        return scores;
    }

    /** Top students by SUM of APPROVED points. Equal totals share a rank. */
    public static List<LeaderboardRow> getLeaderboard(int limit) {
        if (limit <= 0) {
            limit = 10;
        }
        String sql = "SELECT u.user_id, u.name, u.roll_number, SUM(ap.points) AS total "
                   + "FROM users u "
                   + "JOIN activity_points ap ON ap.user_id = u.user_id AND ap.status = ? "
                   + "WHERE u.role = ? "
                   + "GROUP BY u.user_id, u.name, u.roll_number "
                   + "ORDER BY total DESC, u.name ASC "
                   + "LIMIT ?";
        List<LeaderboardRow> rows = new ArrayList<>();
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, Constants.POINTS_APPROVED);
            ps.setString(2, Constants.STUDENT);
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                int position = 0;
                int rank = 0;
                int previousTotal = -1;
                while (rs.next()) {
                    position++;
                    int total = rs.getInt("total");
                    if (total != previousTotal) {
                        rank = position;
                        previousTotal = total;
                    }
                    rows.add(new LeaderboardRow(rank, rs.getInt("user_id"),
                            rs.getString("name"), rs.getString("roll_number"), total));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load leaderboard: " + e.getMessage(), e);
        }
        return rows;
    }

    // ---- helpers ----

    private static int count(String sql) {
        try (PreparedStatement ps = connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read statistics: " + e.getMessage(), e);
        }
    }

    private static Connection connection() {
        return DatabaseConnection.getInstance().getConnection();
    }
}