// src/main/java/com/mecca/service/AdminService.java
package com.mecca.service;

import com.mecca.db.DatabaseConnection;
import com.mecca.model.User;
import com.mecca.util.Constants;
import com.mecca.util.UserFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Club and user administration used by M3's admin panels. All methods static. Owner: M3. */
public class AdminService {

    private AdminService() { }

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

    public static boolean approveClub(int clubId) {
        return update("UPDATE clubs SET status = ? WHERE club_id = ?", Constants.CLUB_APPROVED, clubId);
    }

    /** Reject = delete (schema only allows PENDING/APPROVED). False if the club is still in use. */
    public static boolean rejectClub(int clubId) {
        return update("DELETE FROM clubs WHERE club_id = ?", null, clubId);
    }

    /** Rows: user_id, name, email, role, roll_number. */
    public static List<Object[]> listUsers() {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT user_id, name, email, role, roll_number FROM users ORDER BY user_id";
        try (PreparedStatement ps = conn().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5)});
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load users: " + e.getMessage(), e);
        }
        return rows;
    }

    /** Returns null on success, otherwise a friendly error message. */
    public static String addUser(String name, String email, String password, String role,
                                 String rollNumber, String admissionType) {
        if (isBlank(name) || isBlank(email) || isBlank(password)) {
            return "Name, email and password are required.";
        }
        User u;
        try {
            u = UserFactory.createUser(role);               // Factory pattern from M2
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        u.setName(name.trim());
        u.setEmail(email.trim());
        u.setPasswordHash(AuthService.hashPassword(password));
        String adm = isBlank(admissionType) ? Constants.ADM_REGULAR : admissionType;
        String sql = "INSERT INTO users (name, email, password_hash, role, roll_number, admission_type) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPasswordHash());
            ps.setString(4, u.getRole());
            ps.setString(5, isBlank(rollNumber) ? null : rollNumber.trim());
            ps.setString(6, adm);
            ps.executeUpdate();
            return null;
        } catch (SQLException e) {
            return "Could not add user. The email may already exist.";
        }
    }

    public static boolean changeRole(int userId, String role) {
        if (!Constants.STUDENT.equals(role) && !Constants.ORGANIZER.equals(role)
                && !Constants.ADMIN.equals(role)) {
            return false;
        }
        return update("UPDATE users SET role = ? WHERE user_id = ?", role, userId);
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
        return s == null || s.isBlank();
    }

    private static java.sql.Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }
}