// src/main/java/com/mecca/util/SeedUsers.java
package com.mecca.util;

import com.mecca.db.DatabaseConnection;
import com.mecca.service.AuthService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Inserts 1 admin, 2 organizers and 5 students if the users table is empty. Owner: M2. */
public final class SeedUsers {

    private static final String DEFAULT_PASSWORD = "mecca123";

    // name, email, role, roll number, admission type
    private static final String[][] USERS = {
        {"Admin",          "admin@mecca.test",      "ADMIN",     null,         "REGULAR"},
        {"Organizer One",  "organizer1@mecca.test", "ORGANIZER", null,         "REGULAR"},
        {"Organizer Two",  "organizer2@mecca.test", "ORGANIZER", null,         "REGULAR"},
        {"Student One",    "student1@mecca.test",   "STUDENT",   "MEC24CS001", "REGULAR"},
        {"Student Two",    "student2@mecca.test",   "STUDENT",   "MEC24CS002", "REGULAR"},
        {"Student Three",  "student3@mecca.test",   "STUDENT",   "MEC24CS003", "REGULAR"},
        {"Student Four",   "student4@mecca.test",   "STUDENT",   "MEC24CS004", "LATERAL"},
        {"Student Five",   "student5@mecca.test",   "STUDENT",   "MEC24CS005", "PWD"}
    };

    private SeedUsers() { }

    /** Safe to call on every start; does nothing if any user already exists. */
    public static void seedIfEmpty() {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not check users: " + e.getMessage(), e);
        }

        String sql = "INSERT INTO users (name, email, password_hash, role, roll_number, admission_type) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        String hash = AuthService.hashPassword(DEFAULT_PASSWORD);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String[] u : USERS) {
                ps.setString(1, u[0]);
                ps.setString(2, u[1]);
                ps.setString(3, hash);
                ps.setString(4, u[2]);
                ps.setString(5, u[3]);
                ps.setString(6, u[4]);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not seed users: " + e.getMessage(), e);
        }
    }
}
