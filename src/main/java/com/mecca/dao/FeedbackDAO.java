package com.mecca.dao;

import com.mecca.db.DatabaseConnection;
import com.mecca.model.Feedback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Database access for feedback. Owner: M6. */
public class FeedbackDAO {

    private static Connection con() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public static boolean exists(int eventId, int userId) {
        try (PreparedStatement ps = con().prepareStatement(
                "SELECT 1 FROM feedback WHERE event_id = ? AND user_id = ?")) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not check feedback: " + ex.getMessage(), ex);
        }
    }

    public static void insert(Feedback f) {
        try (PreparedStatement ps = con().prepareStatement(
                "INSERT INTO feedback (event_id, user_id, rating, comments) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, f.getEventId());
            ps.setInt(2, f.getUserId());
            ps.setInt(3, f.getRating());
            ps.setString(4, f.getComments());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not save feedback: " + ex.getMessage(), ex);
        }
    }
}
