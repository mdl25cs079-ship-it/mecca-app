// src/main/java/com/mecca/dao/PointsDAO.java
package com.mecca.dao;

import com.mecca.db.DatabaseConnection;
import com.mecca.model.ActivityPoint;
import com.mecca.util.Constants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Database access for KTU activity points and student claims.
 * Owner: M7.
 */
public class PointsDAO {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static Connection con() {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Checks whether an award row already exists for the given user and event.
     *
     * @param userId  The student ID.
     * @param eventId The event ID.
     * @return true if a row exists, false otherwise.
     */
    public static boolean hasAwardedEvent(int userId, int eventId) {
        String sql = "SELECT 1 FROM activity_points WHERE user_id = ? AND event_id = ? LIMIT 1";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not check awarded event for user " + userId + ", event " + eventId + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * Calculates the total approved points earned by a user under a specific KTU category code.
     *
     * @param userId  The student ID.
     * @param ktuCode The KTU category code.
     * @return Total approved points, or 0 if no records exist.
     */
    public static int sumApprovedPointsByKtuCode(int userId, String ktuCode) {
        if (ktuCode == null || ktuCode.isBlank()) {
            return 0;
        }
        String sql = "SELECT COALESCE(SUM(points), 0) FROM activity_points "
                   + "WHERE user_id = ? AND ktu_code = ? AND status = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, ktuCode.trim());
            ps.setString(3, Constants.POINTS_APPROVED);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not sum approved points for user " + userId + ", ktuCode " + ktuCode + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * Inserts an activity point award or manual claim record.
     *
     * @param userId  The student ID.
     * @param eventId The event ID (null for manual claims).
     * @param ktuCode The KTU category code.
     * @param level   The achievement level (1 to 5).
     * @param points  Points to award.
     * @param status  Status (PENDING or APPROVED).
     * @param note    Optional note or justification.
     */
    public static void insertAward(int userId, Integer eventId, String ktuCode, int level,
                                   int points, String status, String note) {
        if (ktuCode == null || ktuCode.isBlank()) {
            throw new IllegalArgumentException("KTU code cannot be null or blank");
        }
        if (level < 1 || level > 5) {
            throw new IllegalArgumentException("Level must be between 1 and 5 (got " + level + ")");
        }
        if (status == null || (!status.equals(Constants.POINTS_PENDING) && !status.equals(Constants.POINTS_APPROVED))) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Allowed values: "
                    + Constants.POINTS_PENDING + ", " + Constants.POINTS_APPROVED);
        }

        String now = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String sql = "INSERT INTO activity_points (user_id, event_id, ktu_code, level, points, status, awarded_on, certificate_note) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, userId);
            if (eventId == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, eventId);
            }
            ps.setString(3, ktuCode.trim());
            ps.setInt(4, level);
            ps.setInt(5, points);
            ps.setString(6, status);
            ps.setString(7, now);
            if (note == null || note.isBlank()) {
                ps.setNull(8, java.sql.Types.VARCHAR);
            } else {
                ps.setString(8, note.trim());
            }
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not insert activity point award: " + ex.getMessage(), ex);
        }
    }

    /**
     * Returns the activity point history for a student, newest first.
     * Joins events and categories to populate display fields (activityName, groupNo).
     *
     * @param userId The student ID.
     * @return List of ActivityPoint records.
     */
    public static List<ActivityPoint> findHistoryByUserId(int userId) {
        String sql = "SELECT ap.ap_id, ap.user_id, ap.event_id, ap.ktu_code, ap.level, ap.points, "
                   + "       ap.status, ap.awarded_on, ap.certificate_note, "
                   + "       u.name AS student_name, "
                   + "       COALESCE(e.title, kc.name, ap.certificate_note, ap.ktu_code) AS activity_name, "
                   + "       COALESCE(kc.group_no, 0) AS group_no "
                   + "FROM activity_points ap "
                   + "LEFT JOIN users u ON ap.user_id = u.user_id "
                   + "LEFT JOIN events e ON ap.event_id = e.event_id "
                   + "LEFT JOIN ktu_categories kc ON ap.ktu_code = kc.code "
                   + "WHERE ap.user_id = ? "
                   + "ORDER BY ap.awarded_on DESC, ap.ap_id DESC";
        List<ActivityPoint> list = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load activity history for user " + userId + ": " + ex.getMessage(), ex);
        }
        return list;
    }

    /**
     * Returns all pending claims ordered from oldest to newest.
     * Joins users and categories to populate studentName, activityName, and groupNo.
     *
     * @return List of pending ActivityPoint claims.
     */
    public static List<ActivityPoint> findPendingClaims() {
        String sql = "SELECT ap.ap_id, ap.user_id, ap.event_id, ap.ktu_code, ap.level, ap.points, "
                   + "       ap.status, ap.awarded_on, ap.certificate_note, "
                   + "       u.name AS student_name, "
                   + "       COALESCE(e.title, kc.name, ap.certificate_note, ap.ktu_code) AS activity_name, "
                   + "       COALESCE(kc.group_no, 0) AS group_no "
                   + "FROM activity_points ap "
                   + "LEFT JOIN users u ON ap.user_id = u.user_id "
                   + "LEFT JOIN events e ON ap.event_id = e.event_id "
                   + "LEFT JOIN ktu_categories kc ON ap.ktu_code = kc.code "
                   + "WHERE ap.status = ? "
                   + "ORDER BY ap.awarded_on ASC, ap.ap_id ASC";
        List<ActivityPoint> list = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, Constants.POINTS_PENDING);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load pending claims: " + ex.getMessage(), ex);
        }
        return list;
    }

    /**
     * Finds an activity point record by its ID.
     *
     * @param apId The activity point primary key ID.
     * @return The ActivityPoint if found, or null otherwise.
     */
    public static ActivityPoint findById(int apId) {
        String sql = "SELECT ap.ap_id, ap.user_id, ap.event_id, ap.ktu_code, ap.level, ap.points, "
                   + "       ap.status, ap.awarded_on, ap.certificate_note, "
                   + "       u.name AS student_name, "
                   + "       COALESCE(e.title, kc.name, ap.certificate_note, ap.ktu_code) AS activity_name, "
                   + "       COALESCE(kc.group_no, 0) AS group_no "
                   + "FROM activity_points ap "
                   + "LEFT JOIN users u ON ap.user_id = u.user_id "
                   + "LEFT JOIN events e ON ap.event_id = e.event_id "
                   + "LEFT JOIN ktu_categories kc ON ap.ktu_code = kc.code "
                   + "WHERE ap.ap_id = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, apId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not find activity point with id " + apId + ": " + ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * Updates the status and points for an activity point record.
     *
     * @param apId   The activity point ID.
     * @param points Points to assign.
     * @param status The new status (PENDING or APPROVED).
     * @return true if updated, false if row was not found.
     */
    public static boolean updateClaimStatusAndPoints(int apId, int points, String status) {
        if (status == null || (!status.equals(Constants.POINTS_PENDING) && !status.equals(Constants.POINTS_APPROVED))) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Allowed values: "
                    + Constants.POINTS_PENDING + ", " + Constants.POINTS_APPROVED);
        }
        String sql = "UPDATE activity_points SET points = ?, status = ? WHERE ap_id = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, points);
            ps.setString(2, status);
            ps.setInt(3, apId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not update claim " + apId + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * Deletes a claim record if its status is PENDING.
     * Per project schema design, claim rejection removes the pending row.
     *
     * @param apId The claim ID to delete.
     * @return true if deleted, false if not found or not pending.
     */
    public static boolean deleteClaim(int apId) {
        String sql = "DELETE FROM activity_points WHERE ap_id = ? AND status = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, apId);
            ps.setString(2, Constants.POINTS_PENDING);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not delete pending claim " + apId + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * Calculates the sum of approved points per group (Groups 1, 2, and 3).
     *
     * @param userId The student ID.
     * @return int[3] containing sums for Group 1 (index 0), Group 2 (index 1), and Group 3 (index 2).
     */
    public static int[] sumApprovedPointsByGroup(int userId) {
        String sql = "SELECT kc.group_no, COALESCE(SUM(ap.points), 0) AS group_total "
                   + "FROM activity_points ap "
                   + "JOIN ktu_categories kc ON ap.ktu_code = kc.code "
                   + "WHERE ap.user_id = ? AND ap.status = ? "
                   + "GROUP BY kc.group_no";
        int[] totals = new int[3];
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, Constants.POINTS_APPROVED);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int groupNo = rs.getInt("group_no");
                    if (groupNo >= 1 && groupNo <= 3) {
                        totals[groupNo - 1] = rs.getInt("group_total");
                    }
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not sum approved points by group for user " + userId + ": " + ex.getMessage(), ex);
        }
        return totals;
    }

    /**
     * Retrieves the student's admission type from the users table.
     *
     * @param userId The student ID.
     * @return The admission type (e.g. REGULAR, LATERAL, PWD), or null if the user does not exist.
     */
    public static String getAdmissionType(int userId) {
        String sql = "SELECT admission_type FROM users WHERE user_id = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String type = rs.getString("admission_type");
                    return (type != null && !type.isBlank()) ? type.trim() : Constants.ADM_REGULAR;
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load admission type for user " + userId + ": " + ex.getMessage(), ex);
        }
        return null;
    }

    private static ActivityPoint map(ResultSet rs) throws SQLException {
        ActivityPoint ap = new ActivityPoint();
        ap.setApId(rs.getInt("ap_id"));
        ap.setUserId(rs.getInt("user_id"));
        int eventId = rs.getInt("event_id");
        if (rs.wasNull()) {
            ap.setEventId(null);
        } else {
            ap.setEventId(eventId);
        }
        ap.setKtuCode(rs.getString("ktu_code"));
        ap.setLevel(rs.getInt("level"));
        ap.setPoints(rs.getInt("points"));
        ap.setStatus(rs.getString("status"));
        ap.setAwardedOn(rs.getString("awarded_on"));
        ap.setCertificateNote(rs.getString("certificate_note"));

        try {
            ap.setStudentName(rs.getString("student_name"));
        } catch (SQLException ignored) {
            // projection does not include student_name
        }
        try {
            ap.setActivityName(rs.getString("activity_name"));
        } catch (SQLException ignored) {
            // projection does not include activity_name
        }
        try {
            ap.setGroupNo(rs.getInt("group_no"));
        } catch (SQLException ignored) {
            // projection does not include group_no
        }

        return ap;
    }
}
