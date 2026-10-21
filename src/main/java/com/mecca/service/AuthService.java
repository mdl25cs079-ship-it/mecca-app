// src/main/java/com/mecca/service/AuthService.java
package com.mecca.service;

import com.mecca.db.DatabaseConnection;
import com.mecca.model.Organizer;
import com.mecca.model.Student;
import com.mecca.model.User;
import com.mecca.util.UserFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Authentication logic. All methods are static. Owner: M2. */
public class AuthService {
    private static User currentUser;

    /** Returns the user if email and password match, otherwise empty. Throws IllegalStateException on DB errors. */
    public static Optional<User> login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isEmpty()) {
            return Optional.empty();
        }
        String sql = "SELECT user_id, name, email, password_hash, role, roll_number, "
                   + "admission_type, managed_club_id FROM users WHERE LOWER(email) = LOWER(?)";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                byte[] stored = rs.getString("password_hash").getBytes(StandardCharsets.UTF_8);
                byte[] given = hashPassword(password).getBytes(StandardCharsets.UTF_8);
                if (!MessageDigest.isEqual(stored, given)) {
                    return Optional.empty();
                }
                User user = buildUser(rs);
                setCurrentUser(user);
                return Optional.of(user);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not log in: " + e.getMessage(), e);
        }
    }

    /** Self sign-up for students. Returns false if the email is already used or input is missing. */
    public static boolean registerStudent(String name, String email, String password,
                                          String rollNumber, String admissionType) {
        if (isBlank(name) || isBlank(email) || isBlank(password)) {
            return false;
        }
        String type = "REGULAR";
        if (admissionType != null) {
            String t = admissionType.trim().toUpperCase();
            if (t.equals("REGULAR") || t.equals("LATERAL") || t.equals("PWD")) {
                type = t;
            }
        }
        String exists = "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?)";
        String insert = "INSERT INTO users (name, email, password_hash, role, roll_number, admission_type) "
                      + "VALUES (?, ?, ?, 'STUDENT', ?, ?)";
        try (PreparedStatement check = connection().prepareStatement(exists)) {
            check.setString(1, email.trim());
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    return false;
                }
            }
            try (PreparedStatement ps = connection().prepareStatement(insert)) {
                ps.setString(1, name.trim());
                ps.setString(2, email.trim());
                ps.setString(3, hashPassword(password));
                ps.setString(4, isBlank(rollNumber) ? null : rollNumber.trim());
                ps.setString(5, type);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not register student: " + e.getMessage(), e);
        }
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User u) {
        currentUser = u;
    }

    /** SHA-256 of the password as a lowercase hex string. */
    public static String hashPassword(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }


    private static Connection connection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /** Uses the factory to create the right subclass, then fills its fields. */
    private static User buildUser(ResultSet rs) throws SQLException {
        User u = UserFactory.createUser(rs.getString("role"));
        u.setUserId(rs.getInt("user_id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        if (u instanceof Student) {
            Student s = (Student) u;
            s.setRollNumber(rs.getString("roll_number"));
            s.setAdmissionType(rs.getString("admission_type"));
        } else if (u instanceof Organizer) {
            int clubId = rs.getInt("managed_club_id");
            if (!rs.wasNull()) {
                ((Organizer) u).setManagedClubId(clubId);
            }
        }
        return u;
    }
}