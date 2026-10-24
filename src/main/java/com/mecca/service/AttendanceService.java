// src/main/java/com/mecca/service/AttendanceService.java
package com.mecca.service;

import com.mecca.dao.AttendanceDAO;
import com.mecca.model.Attendance;
import com.mecca.model.RegistrantRow;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/** Business logic for marking and reading attendance. */
public class AttendanceService {

    private static final AttendanceDAO dao = new AttendanceDAO();

    /** Registered students of an event with an attended flag. */
    public static List<RegistrantRow> getRegistrantsForEvent(int eventId) {
        try {
            return dao.findRegistrantsByEvent(eventId);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load registrants: " + e.getMessage(), e);
        }
    }

    /** Marks a registration present. Does nothing if already marked. */
    public static void markAttendance(int regId) {
        try {
            if (dao.existsByRegId(regId)) {
                return; // already marked, ignore
            }
            dao.insert(new Attendance(0, regId, LocalDateTime.now(), true));
        } catch (SQLException e) {
            throw new IllegalStateException("Could not mark attendance: " + e.getMessage(), e);
        }

        // Attendance is saved. A points error must never undo it.
        try {
            int[] ids = dao.findUserAndEventByRegId(regId); // {userId, eventId}
            if (ids != null) {
                ActivityPointsService.awardPoints(ids[0], ids[1]);
            }
        } catch (Exception e) {
            System.err.println("Points not awarded for reg " + regId + ": " + e.getMessage());
        }
    }

    /** "PRESENT" if the student attended the event, otherwise an empty string. */
    public static String getStatus(int userId, int eventId) {
        
            return AttendanceDAO.hasAttended(userId, eventId) ? "PRESENT" : "";
        
    }
}