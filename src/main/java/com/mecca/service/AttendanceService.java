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
        /** "PRESENT" if the student attended the event, otherwise an empty string. */
    public static String getStatus(int userId, int eventId) {
        return AttendanceDAO.hasAttended(userId, eventId) ? "PRESENT" : "";
    }
}
