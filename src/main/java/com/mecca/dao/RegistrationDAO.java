package com.mecca.dao;

import com.mecca.db.DatabaseConnection;
import com.mecca.model.Registration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC data access for the registrations table (SQLite via the shared
 * DatabaseConnection singleton).
 *
 * Assumed table (check against schema.sql):
 *   registrations(reg_id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER,
 *                 event_id INTEGER, reg_date TIMESTAMP/TEXT, status TEXT)
 *
 * IMPORTANT: the Connection is shared and owned by DatabaseConnection, so it
 * is never closed here. Only statements and result sets are closed.
 */
public class RegistrationDAO {

    private static final String INSERT_SQL =
            "INSERT INTO registrations (user_id, event_id, reg_date, status) VALUES (?, ?, ?, ?)";

    private static final String EXISTS_SQL =
            "SELECT 1 FROM registrations WHERE user_id = ? AND event_id = ? AND status <> 'CANCELLED'";

    private static final String COUNT_CONFIRMED_SQL =
            "SELECT COUNT(*) FROM registrations WHERE event_id = ? AND status = 'CONFIRMED'";

    private static final String FIND_BY_USER_SQL =
            "SELECT reg_id, user_id, event_id, reg_date, status FROM registrations "
                    + "WHERE user_id = ? ORDER BY reg_date DESC";

    private static final String FIND_REG_ID_SQL =
            "SELECT reg_id FROM registrations WHERE user_id = ? AND event_id = ? "
                    + "ORDER BY reg_date DESC";

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Inserts a registration and sets the generated regId on the object.
     * @return true if a row was inserted
     */
    public boolean insert(Registration reg) throws SQLException {
        LocalDateTime when = reg.getRegDate() != null ? reg.getRegDate() : LocalDateTime.now();
        String status = reg.getStatus() != null ? reg.getStatus() : Registration.STATUS_CONFIRMED;

        try (PreparedStatement ps = conn().prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reg.getUserId());
            ps.setInt(2, reg.getEventId());
            ps.setTimestamp(3, Timestamp.valueOf(when));
            ps.setString(4, status);

            int rows = ps.executeUpdate();
            if (rows > 0) {
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
        Timestamp ts = rs.getTimestamp("reg_date");
        r.setRegDate(ts != null ? ts.toLocalDateTime() : null);
        r.setStatus(rs.getString("status"));
        return r;
    }
}