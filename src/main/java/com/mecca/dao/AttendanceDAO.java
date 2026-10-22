package com.mecca.dao;

import com.mecca.db.DatabaseConnection;
import com.mecca.model.Event;
import com.mecca.model.RegistrantRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Database access for attendance. Owner: M6. */
public class AttendanceDAO {

    private static Connection con() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private static Event mapEvent(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setEventId(rs.getInt("event_id"));
        e.setClubId(rs.getInt("club_id"));
        e.setTitle(rs.getString("title"));
        e.setDescription(rs.getString("description"));
        e.setDateTime(rs.getString("date_time"));
        e.setCapacity(rs.getInt("capacity"));
        e.setVenueId(rs.getInt("venue_id"));
        e.setKtuCode(rs.getString("ktu_code"));
        e.setLevel(rs.getInt("level"));
        e.setStatus(rs.getString("status"));
        return e;
    }

    /** Non-cancelled events of a club (newest first). clubId <= 0 means all clubs (admin or testing). */
    public static List<Event> findEventsByClub(int clubId) {
        String sql = "SELECT * FROM events WHERE status <> 'CANCELLED'"
                + (clubId > 0 ? " AND club_id = ?" : "") + " ORDER BY date_time DESC";
        List<Event> list = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            if (clubId > 0) ps.setInt(1, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapEvent(rs));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load events: " + ex.getMessage(), ex);
        }
        return list;
    }

    /** Confirmed registrants of an event, with a flag showing whether attendance is already marked. */
    public static List<RegistrantRow> findRegistrants(int eventId) {
        String sql = "SELECT r.reg_id, r.user_id, u.name, u.roll_number, "
                + "CASE WHEN a.att_id IS NULL THEN 0 ELSE 1 END AS attended "
                + "FROM registrations r "
                + "JOIN users u ON u.user_id = r.user_id "
                + "LEFT JOIN attendance a ON a.reg_id = r.reg_id "
                + "WHERE r.event_id = ? AND r.status = 'CONFIRMED' ORDER BY u.name";
        List<RegistrantRow> list = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RegistrantRow row = new RegistrantRow();
                    row.setRegId(rs.getInt("reg_id"));
                    row.setUserId(rs.getInt("user_id"));
                    row.setStudentName(rs.getString("name"));
                    row.setRollNumber(rs.getString("roll_number"));
                    row.setAttended(rs.getInt("attended") == 1);
                    list.add(row);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load registrants: " + ex.getMessage(), ex);
        }
        return list;
    }

    public static boolean isMarked(int regId) {
        try (PreparedStatement ps = con().prepareStatement("SELECT 1 FROM attendance WHERE reg_id = ?")) {
            ps.setInt(1, regId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not check attendance: " + ex.getMessage(), ex);
        }
    }

    /** Returns {userId, eventId} for a registration, or null if it does not exist. */
    public static int[] findUserAndEvent(int regId) {
        try (PreparedStatement ps = con().prepareStatement(
                "SELECT user_id, event_id FROM registrations WHERE reg_id = ?")) {
            ps.setInt(1, regId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new int[]{rs.getInt("user_id"), rs.getInt("event_id")};
                return null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not read registration: " + ex.getMessage(), ex);
        }
    }

    public static void insert(int regId) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try (PreparedStatement ps = con().prepareStatement(
                "INSERT INTO attendance (reg_id, check_in_time, verified) VALUES (?, ?, 1)")) {
            ps.setInt(1, regId);
            ps.setString(2, now);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not save attendance: " + ex.getMessage(), ex);
        }
    }

    public static boolean hasAttended(int userId, int eventId) {
        String sql = "SELECT 1 FROM attendance a JOIN registrations r ON r.reg_id = a.reg_id "
                + "WHERE r.user_id = ? AND r.event_id = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not check attendance: " + ex.getMessage(), ex);
        }
    }

    /** Events the student actually attended (newest first). */
    public static List<Event> findAttendedEvents(int userId) {
        String sql = "SELECT e.* FROM events e "
                + "JOIN registrations r ON r.event_id = e.event_id "
                + "JOIN attendance a ON a.reg_id = r.reg_id "
                + "WHERE r.user_id = ? ORDER BY e.date_time DESC";
        List<Event> list = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapEvent(rs));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load attended events: " + ex.getMessage(), ex);
        }
        return list;
    }
}
