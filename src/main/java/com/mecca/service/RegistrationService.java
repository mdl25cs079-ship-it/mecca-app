package com.mecca.service;

import com.mecca.dao.RegistrationDAO;
import com.mecca.model.Event;
import com.mecca.model.Registration;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Business rules for student event registration.
 *
 * Contracts assumed from other modules (all static, same package com.mecca.service):
 *   Event  EventService.getEventById(int eventId)   -> null if not found
 *   Event  has getEventId(), getCapacity(), getStatus() ("ACTIVE")
 *   void   NotificationService.sendConfirmation(int userId, int eventId)
 */
public class RegistrationService {

    private static final String EVENT_ACTIVE = "ACTIVE";

    private final RegistrationDAO dao = new RegistrationDAO();

    /**
     * Registers a student for an event.
     *
     * @return true only when a CONFIRMED registration was inserted.
     *         Returns false (no exception) when the event is missing/inactive,
     *         the student is already registered, the event is full,
     *         or a database error occurs.
     */
    public boolean register(int userId, int eventId) {
        try {
            Event event = EventService.getEventById(eventId);
            if (event == null || !EVENT_ACTIVE.equalsIgnoreCase(event.getStatus())) {
                return false;
            }

            if (dao.exists(userId, eventId)) {
                return false;
            }

            if (dao.countConfirmed(eventId) >= event.getCapacity()) {
                return false;
            }

            Registration reg = new Registration(userId, eventId); // CONFIRMED, now
            boolean inserted = dao.insert(reg);
            if (!inserted) {
                return false;
            }

            // Notification failure must never undo or fail the registration.
            try {
                NotificationService.sendConfirmation(userId, eventId);
            } catch (Exception e) {
                System.err.println("Confirmation notification failed for user "
                        + userId + ", event " + eventId + ": " + e.getMessage());
            }
            return true;

        } catch (SQLException e) {
            System.err.println("Registration failed for user " + userId
                    + ", event " + eventId + ": " + e.getMessage());
            return false;
        }
    }

    /** All registrations of the student, newest first. Empty list on error. */
    public List<Registration> getHistory(int userId) {
        try {
            return dao.findByUser(userId);
        } catch (SQLException e) {
            System.err.println("Could not load history for user " + userId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** The regId for this student and event, or -1 if not registered. */
    public int getRegId(int userId, int eventId) {
        try {
            return dao.findRegId(userId, eventId);
        } catch (SQLException e) {
            System.err.println("Could not find regId for user " + userId
                    + ", event " + eventId + ": " + e.getMessage());
            return -1;
        }
    }

    // ---- Helpers for the UI (so panels can show the right message) ----

    /** True if the student already holds a non-cancelled registration. */
    public boolean isAlreadyRegistered(int userId, int eventId) {
        try {
            return dao.exists(userId, eventId);
        } catch (SQLException e) {
            System.err.println("Duplicate check failed: " + e.getMessage());
            return false;
        }
    }

    /** Seats remaining (capacity minus confirmed), never below 0; 0 if event not found. */
    public int getSeatsLeft(int eventId) {
        try {
            Event event = EventService.getEventById(eventId);
            if (event == null) {
                return 0;
            }
            return Math.max(0, event.getCapacity() - dao.countConfirmed(eventId));
        } catch (SQLException e) {
            System.err.println("Seat count failed for event " + eventId + ": " + e.getMessage());
            return 0;
        }
    }
}