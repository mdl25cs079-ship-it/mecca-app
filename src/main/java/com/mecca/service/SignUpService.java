package com.mecca.service;

import com.mecca.db.DatabaseConnection;
import com.mecca.util.Constants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SignUpService {

    public static String registerStudent(String name, String email, String password,
                                         String rollNumber, String admissionType) {
        if (name == null || name.trim().isEmpty()) return "Name is required.";
        if (email == null || !email.trim().matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$"))
            return "Enter a valid email address.";
        if (password == null || password.length() < 6) return "Password must be at least 6 characters.";
        if (rollNumber == null || rollNumber.trim().isEmpty()) return "Roll number is required.";

        String adm = admissionType;
        if (!Constants.ADM_REGULAR.equals(adm) && !Constants.ADM_LATERAL.equals(adm)
                && !Constants.ADM_PWD.equals(adm)) {
            adm = Constants.ADM_REGULAR;
        }

        String sql = "INSERT INTO users (name, email, password_hash, role, roll_number, admission_type) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        Connection con = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, AuthService.hashPassword(password));
            ps.setString(4, Constants.STUDENT);
            ps.setString(5, rollNumber.trim().toUpperCase());
            ps.setString(6, adm);
            ps.executeUpdate();
            return null;
        } catch (SQLException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("UNIQUE")) return "An account with this email already exists.";
            return "Could not create account: " + msg;
        }
    }
}