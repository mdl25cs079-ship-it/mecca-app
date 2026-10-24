// src/main/java/com/mecca/service/AdminService.java
package com.mecca.service;

import com.mecca.db.DatabaseConnection;
import com.mecca.model.User;
import com.mecca.util.Constants;
import com.mecca.util.UserFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Club and user administration used by the admin panels. All methods static. Owner: M3 (updated by M1). */
public class AdminService {

    private AdminService() { }

    // ------------------------------------------------------------------ clubs

    /** Rows: club_id, name, description, status. */
    public static List<Object[]> listClubs() {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT club_id, name, description, status FROM clubs ORDER BY club_id";
        try (PreparedStatement ps = conn().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)});
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load clubs: " + e.getMessage(), e);
        }
        return rows;
    }

    /** Rows: club_id, name (for drop-down lists). */
    public static List<Object[]> listClubChoices() {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT club_id, name FROM clubs ORDER BY name";
        try (PreparedStatement ps = conn().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{rs.getInt(1), rs.getString(2)});
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load clubs: " + e.getMessage(), e);
        }
        return rows;
    }

    /** Creates an APPROVED club. Returns null on success, otherwise a friendly error message. */
    public static String addClub(String name, String description) {
        if (isBlank(name)) {
            return "Club name is required.";
        }
        String sql = "INSERT INTO clubs (name, description, status) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, isBlank(description) ? null : description.trim());
            ps.setString(3, Constants.CLUB_APPROVED);
            ps.executeUpdate();
            return null;
        } catch (SQLException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("UNIQUE")) {
                return "A club with this name already exists.";
            }
            return "Could not add the club: " + msg;
        }
    }

    public static boolean approveClub(int clubId) {
        return update("UPDATE clubs SET status = ? WHERE club_id = ?", Constants.CLUB_APPROVED, clubId);
    }

    /** Reject = delete (schema only allows PENDING/APPROVED). False if the club is still in use. */
    public static boolean rejectClub(int clubId) {
        return update("DELETE FROM clubs WHERE club_id = ?", null, clubId);
    }

    // ------------------------------------------------------------------ users

    /** Rows: user_id, name, email, role, roll_number, club name (or "-"). */
    public static List<Object[]> listUsers() {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT u.user_id, u.name, u.email, u.role, u.roll_number, COALESCE(c.name, '-') "
                   + "FROM users u LEFT JOIN clubs c ON c.club_id = u.managed_club_id "
                   + "ORDER BY u.user_id";
        try (PreparedStatement ps = conn().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getString(6)});
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load users: " + e.getMessage(), e);
        }
        return rows;
    }

    /** Old signature kept so existing callers still compile. */
    public static String addUser(String name, String email, String password, String role,
                                 String rollNumber, String admissionType) {
        return addUser(name, email, password, role, rollNumber, admissionType, null);
    }

    /** Returns null on success, otherwise a friendly error message. clubId is used only for organizers. */
    public static String addUser(String name, String email, String password, String role,
                                 String rollNumber, String admissionType, Integer clubId) {
        if (isBlank(name) || isBlank(email) || isBlank(password)) {
            return "Name, email and password are required.";
        }
        if (password.length() < 6) {
            return "Password must be at least 6 characters.";
        }
        User u;
        try {
            u = UserFactory.createUser(role);               // Factory pattern from M2
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        u.setName(name.trim());
        u.setEmail(email.trim().toLowerCase());
        u.setPasswordHash(AuthService.hashPassword(password));
        String adm = isBlank(admissionType) ? Constants.ADM_REGULAR : admissionType;
        boolean organizer = Constants.ORGANIZER.equals(u.getRole());
        String sql = "INSERT INTO users (name, email, password_hash, role, roll_number, admission_type, managed_club_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPasswordHash());
            ps.setString(4, u.getRole());
            ps.setString(5, isBlank(rollNumber) ? null : rollNumber.trim().toUpperCase());
            ps.setString(6, adm);
            if (organizer && clubId != null) {
                ps.setInt(7, clubId);
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.executeUpdate();
            return null;
        } catch (SQLException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("UNIQUE")) {
                return "An account with this email already exists.";
            }
            return "Could not add user: " + msg;
        }
    }

    /** Changes a role. Anyone who is no longer an organizer loses their club link. */
    public static boolean changeRole(int userId, String role) {
        if (!Constants.STUDENT.equals(role) && !Constants.ORGANIZER.equals(role)
                && !Constants.ADMIN.equals(role)) {
            return false;
        }
        String sql = "UPDATE users SET role = ?, "
                   + "managed_club_id = CASE WHEN ? = 'ORGANIZER' THEN managed_club_id ELSE NULL END "
                   + "WHERE user_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setString(2, role);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /** Links an organizer to a club (clubId null removes the link). Only organizers can be linked. */
    public static boolean assignClub(int userId, Integer clubId) {
        String sql = "UPDATE users SET managed_club_id = ? WHERE user_id = ? AND role = 'ORGANIZER'";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            if (clubId == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, clubId);
            }
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /** False if the user has registrations or points (foreign keys). */
    public static boolean deleteUser(int userId) {
        return update("DELETE FROM users WHERE user_id = ?", null, userId);
    }

    // ---- helpers ----

    /** Runs an update with an optional text parameter first, then the id. Returns false on SQL error. */
    private static boolean update(String sql, String text, int id) {
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            int i = 1;
            if (text != null) {
                ps.setString(i++, text);
            }
            ps.setInt(i, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static java.sql.Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }
}