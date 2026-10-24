// src/main/java/com/mecca/service/FeedbackService.java
package com.mecca.service;

import com.mecca.dao.AttendanceDAO;
import com.mecca.dao.FeedbackDAO;
import com.mecca.model.Feedback;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Business logic for event feedback. */
public class FeedbackService {

    private static final FeedbackDAO feedbackDAO = new FeedbackDAO();
    private static final AttendanceDAO attendanceDAO = new AttendanceDAO();

    /**
     * Saves feedback. Returns false if the rating is not 1..5,
     * the student did not attend, feedback was already given, or a DB error occurs.
     */
    public static boolean submit(int eventId, int userId, int rating, String comments) {
        if (rating < 1 || rating > 5) {
            return false;
        }
        try {
            if (!attendanceDAO.hasAttended(userId, eventId)) {
                return false;
            }
            if (feedbackDAO.exists(eventId, userId)) {
                return false;
            }
            String text = (comments == null) ? "" : comments.trim();
            feedbackDAO.insert(new Feedback(0, eventId, userId, rating, text));
            return true;
        } catch (SQLException e) {
            System.err.println("Could not save feedback: " + e.getMessage());
            return false;
        }
    }

    /** True if the student attended the event. */
    public static String hasAttended(int userId, int eventId) {
    
            return AttendanceDAO.hasAttended(userId, eventId) ? "PRESENT" : ""; 
        
    }

    /** True if the student already submitted feedback for the event. */
    public static boolean hasSubmitted(int eventId, int userId) {
        try {
            return feedbackDAO.exists(eventId, userId);
        } catch (SQLException e) {
            System.err.println("Could not check feedback: " + e.getMessage());
            return false;
        }
    }

    /** Ids of events the student attended (used by FeedbackPanel). */
    public static List<Integer> getAttendedEventIds(int userId) {
        try {
            return attendanceDAO.findAttendedEventIds(userId);
        } catch (SQLException e) {
            System.err.println("Could not load attended events: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}