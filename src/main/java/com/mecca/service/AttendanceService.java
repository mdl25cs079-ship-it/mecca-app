package com.mecca.service;

import com.mecca.dao.AttendanceDAO;
import com.mecca.model.Event;
import com.mecca.model.RegistrantRow;

import java.util.List;

/** Attendance logic. Owner: M6. All methods are static. */
public class AttendanceService {

    /** Confirmed registrants of an event with their attended flag. */
    public static List<RegistrantRow> getRegistrantsForEvent(int eventId) {
        return AttendanceDAO.findRegistrants(eventId);
    }

    /** Extra helper (not in the contract): events an organizer can take attendance for. clubId <= 0 = all clubs. */
    public static List<Event> getEventsForAttendance(int clubId) {
        return AttendanceDAO.findEventsByClub(clubId);
    }

    /**
     * Marks one registration as present, then awards KTU points.
     * Ignores a registration that is already marked. A points error never loses the attendance record.
     */
    public static void markAttendance(int regId) {
        if (AttendanceDAO.isMarked(regId)) {
            return;
        }
        int[] userAndEvent = AttendanceDAO.findUserAndEvent(regId);
        if (userAndEvent == null) {
            throw new IllegalArgumentException("Registration " + regId + " does not exist.");
        }
        AttendanceDAO.insert(regId);
        try {
            ActivityPointsService.awardPoints(userAndEvent[0], userAndEvent[1]);
        } catch (Exception ex) {
            System.err.println("Attendance saved but awarding points failed: " + ex.getMessage());
        }
    }

    /**
     * Returns the attendance status string for a student's event registration.
     *
     * Contract (documented in HistoryPanel.java):
     *   String AttendanceService.getStatus(int userId, int eventId)
     *   Returns null or empty string if attendance has not been marked.
     *
     * Fix (2026-10-24): Added to satisfy the M5 HistoryPanel contract.
     * Delegates to AttendanceDAO.hasAttended() which performs the correct
     * JOIN between attendance and registrations tables.
     *
     * @param userId  the student user_id
     * @param eventId the event_id
     * @return "Attended" if a verified attendance record exists, null otherwise
     */
    public static String getStatus(int userId, int eventId) {
        try {
            return AttendanceDAO.hasAttended(userId, eventId) ? "Attended" : null;
        } catch (Exception ex) {
            System.err.println("Could not load attendance status for user "
                    + userId + ", event " + eventId + ": " + ex.getMessage());
            return null;
        }
    }
}
