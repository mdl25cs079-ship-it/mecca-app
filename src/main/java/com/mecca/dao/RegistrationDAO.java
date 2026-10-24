// src/main/java/com/mecca/dao/RegistrationDAO.java
package com.mecca.dao;

import com.mecca.db.DatabaseConnection;
import com.mecca.model.Registration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC data access for the registrations table.
 *
 * FIX: reg_date is a TEXT column. The old code wrote and read java.sql.Timestamp, which SQLite stores
 * as a number and then fails to parse back ("Error parsing time stamp"). That made getHistory()
 * return an empty list for every student. Dates are now written as text ("yyyy-MM-dd HH:mm:ss")
 * and read back with a tolerant parser that also understands older rows.
 *
 * The Connection is shared and owned by DatabaseConnection, so it is never closed here.
 */
public class RegistrationDAO {

    private static final DateTimeFormatter DB_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String INSERT_SQL =
            "INSERT INTO registrations (user_id, event_id, reg_date, status) VALUES (?, ?, ?, ?)";

    private static final String EXISTS_SQL =
            "SELECT 1 FROM registrations WHERE user_id = ? AND event_id = ? AND status <> 'CANCELLED'";

    private static final String COUNT_CONFIRMED_SQL =
            "SELECT COUNT(*) FROM registrations WHERE event_id = ? AND status = 'CONFIRMED'";

    private static final String FIND_BY_USER_SQL =
            "SELECT reg_id, user_id, event_id, reg_date, status FROM registrations "
                    + "WHERE user_id = ? ORDER BY reg_id DESC";

    private static final String FIND_REG_ID_SQL =
            "SELECT reg_id FROM registrations WHERE user_id = ? AND event_id = ? ORDER BY reg_id DESC";

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    /** Inserts a registration and sets the generated regId on the object. */
    public boolean insert(Registration reg) throws SQLException {
        LocalDateTime when = reg.getRegDate() != null ? reg.getRegDate() : LocalDateTime.now();
        String status = reg.getStatus() != null ? reg.getStatus() : Registration.STATUS_CONFIRMED;

        try (PreparedStatement ps = conn().prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reg.getUserId());
            ps.setInt(2, reg.getEventId());
            ps.setString(3, when.format(DB_FORMAT));
            ps.setString(4, status);

            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        reg.setRegId(keys.getInt(1));
                    }
                }
                reg.setRegDate(when);
                reg.setStatus(status);
                return true;
            }
            return false;
        }
    }

    /** True if the user already has a non-cancelled registration for the event. */
    public boolean exists(int userId, int eventId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(EXISTS_SQL)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Number of CONFIRMED registrations for the event. */
    public int countConfirmed(int eventId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(COUNT_CONFIRMED_SQL)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** All registrations for a user, newest first. Empty list if none. */
    public List<Registration> findByUser(int userId) throws SQLException {
        List<Registration> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(FIND_BY_USER_SQL)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /** The regId of the user's most recent registration for the event, or -1 if none. */
    public int findRegId(int userId, int eventId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(FIND_REG_ID_SQL)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("reg_id") : -1;
            }
        }
    }

    private Registration mapRow(ResultSet rs) throws SQLException {
        Registration r = new Registration();
        r.setRegId(rs.getInt("reg_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setEventId(rs.getInt("event_id"));
        r.setRegDate(parseDate(rs.getString("reg_date")));
        r.setStatus(rs.getString("status"));
        return r;
    }

    /** Understands "yyyy-MM-dd HH:mm:ss(.SSS)", "yyyy-MM-dd HH:mm", ISO "T" dates, plain dates and epoch numbers. */
    static LocalDateTime parseDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String t = raw.trim();
        String[] patterns = {"yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm"};
        for (String p : patterns) {
            try {
                return LocalDateTime.parse(t, DateTimeFormatter.ofPattern(p));
            } catch (DateTimeParseException ignored) {
                // try the next format
            }
        }
        try {
            return LocalDateTime.parse(t);
        } catch (DateTimeParseException ignored) {
            // try the next format
        }
        try {
            return LocalDate.parse(t).atStartOfDay();
        } catch (DateTimeParseException ignored) {
            // try the next format
        }
        try {
            long v = Long.parseLong(t);
            if (v < 100000000000L) {
                v *= 1000; // seconds -> milliseconds
            }
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(v), ZoneId.systemDefault());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}