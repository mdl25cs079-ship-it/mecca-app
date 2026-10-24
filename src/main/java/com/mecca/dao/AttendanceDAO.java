// src/main/java/com/mecca/dao/AttendanceDAO.java
package com.mecca.dao;

import com.mecca.db.DatabaseConnection;
import com.mecca.model.Attendance;
import com.mecca.model.RegistrantRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Database access for the attendance table. */
public class AttendanceDAO {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** True if an attendance row already exists for this registration. */
    public boolean existsByRegId(int regId) throws SQLException {
        String sql = "SELECT 1 FROM attendance WHERE reg_id = ?";
        Connection con = DatabaseConnection.getInstance().getConnection(); // shared: do not close
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, regId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Inserts an attendance row and returns the new att_id. */
    public int insert(Attendance a) throws SQLException {
        String sql = "INSERT INTO attendance (reg_id, check_in_time, verified) VALUES (?, ?, ?)";
        Connection con = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getRegId());
            ps.setString(2, a.getCheckInTime().format(FMT));
            ps.setInt(3, a.isVerified() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    /** Returns {userId, eventId} for a registration, or null if not found. */
    public int[] findUserAndEventByRegId(int regId) throws SQLException {
        String sql = "SELECT user_id, event_id FROM registrations WHERE reg_id = ?";
        Connection con = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, regId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new int[] { rs.getInt("user_id"), rs.getInt("event_id") };
                }
                return null;
            }
        }
    }

    /** All confirmed registrants of an event, with a flag for who already attended. */
    public List<RegistrantRow> findRegistrantsByEvent(int eventId) throws SQLException {
        String sql = "SELECT r.reg_id, u.name, u.roll_number, "
                   + "CASE WHEN a.att_id IS NULL THEN 0 ELSE 1 END AS attended "
                   + "FROM registrations r "
                   + "JOIN users u ON u.user_id = r.user_id "
                   + "LEFT JOIN attendance a ON a.reg_id = r.reg_id "
                   + "WHERE r.event_id = ? AND r.status = 'CONFIRMED' "
                   + "ORDER BY u.name";
        List<RegistrantRow> rows = new ArrayList<>();
        Connection con = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String roll = rs.getString("roll_number");
                    rows.add(new RegistrantRow(
                            rs.getInt("reg_id"),
                            rs.getString("name"),
                            roll == null ? "-" : roll,
                            rs.getInt("attended") == 1));
                }
            }
        }
        return rows;
    }

    /** True if this student has a verified attendance record for this event. */
    public static boolean hasAttended(int userId, int eventId) {
        String sql = "SELECT 1 FROM attendance a "
                   + "JOIN registrations r ON r.reg_id = a.reg_id "
                   + "WHERE r.user_id = ? AND r.event_id = ? AND a.verified = 1 LIMIT 1";
        try{
            Connection con = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setInt(2, eventId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            System.err.println("Could not check attendance: " + e.getMessage());
            return false;
        }
    }

    /** Ids of all events this student attended. */
    public List<Integer> findAttendedEventIds(int userId) throws SQLException {
        String sql = "SELECT DISTINCT r.event_id FROM attendance a "
                   + "JOIN registrations r ON r.reg_id = a.reg_id "
                   + "WHERE r.user_id = ? AND a.verified = 1 ORDER BY r.event_id";
        List<Integer> ids = new ArrayList<>();
        Connection con = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("event_id"));
                }
            }
        }
        return ids;
    }
}