// src/main/java/com/mecca/dao/FeedbackDAO.java
package com.mecca.dao;

import com.mecca.db.DatabaseConnection;
import com.mecca.model.Feedback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Database access for the feedback table. */
public class FeedbackDAO {

    /** True if this student already gave feedback for this event. */
    public boolean exists(int eventId, int userId) throws SQLException {
        String sql = "SELECT 1 FROM feedback WHERE event_id = ? AND user_id = ?";
        Connection con = DatabaseConnection.getInstance().getConnection(); // shared: do not close
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Inserts a feedback row and returns the new feedback_id. */
    public int insert(Feedback f) throws SQLException {
        String sql = "INSERT INTO feedback (event_id, user_id, rating, comments) VALUES (?, ?, ?, ?)";
        Connection con = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, f.getEventId());
            ps.setInt(2, f.getUserId());
            ps.setInt(3, f.getRating());
            ps.setString(4, f.getComments());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }
}