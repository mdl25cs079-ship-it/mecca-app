package com.mecca.service;

import com.mecca.dao.AttendanceDAO;
import com.mecca.dao.FeedbackDAO;
import com.mecca.model.Event;
import com.mecca.model.Feedback;

import java.util.List;

/** Feedback logic. Owner: M6. All methods are static. */
public class FeedbackService {

    public static boolean hasAttended(int userId, int eventId) {
        return AttendanceDAO.hasAttended(userId, eventId);
    }

    public static boolean hasSubmitted(int eventId, int userId) {
        return FeedbackDAO.exists(eventId, userId);
    }

    /** Events this student attended, so they can rate them. */
    public static List<Event> getAttendedEvents(int userId) {
        return AttendanceDAO.findAttendedEvents(userId);
    }

    /** Returns false if the rating is not 1-5, the student did not attend, or feedback was already given. */
    public static boolean submit(int eventId, int userId, int rating, String comments) {
        if (rating < 1 || rating > 5) return false;
        if (!hasAttended(userId, eventId)) return false;
        if (hasSubmitted(eventId, userId)) return false;
        Feedback f = new Feedback();
        f.setEventId(eventId);
        f.setUserId(userId);
        f.setRating(rating);
        f.setComments(comments == null ? "" : comments.trim());
        FeedbackDAO.insert(f);
        return true;
    }
}
