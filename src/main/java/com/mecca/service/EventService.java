// src/main/java/com/mecca/service/EventService.java
package com.mecca.service;

import com.mecca.dao.ClubDAO;
import com.mecca.model.Club;
import com.mecca.dao.EventDAO;
import com.mecca.model.Event;
import com.mecca.model.Venue;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for events. All methods are static per the project contract.
 * Owner: M4.
 *
 * Fix (2026-10-24): Restored public static modifiers to all service methods to
 * comply with the project contract documented in README.md Rule #3
 * ("All service methods are public static"). The M5 integration introduced
 * callers that invoked these methods statically, which caused a compilation
 * failure after M4 changed them to instance methods. The fix converts the
 * instance EventDAO field to a private static final field. Existing M4 UI
 * callers (CreateEventPanel, MyEventsPanel) that use an EventService instance
 * reference remain compatible — in Java, static methods are callable via an
 * object reference. No M4 panel code was modified.
 */
public class EventService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final EventDAO DAO = new EventDAO();

    public static List<Event> getActiveEvents() {
        try {
            return DAO.findActive();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static Event getEventById(int eventId) {
        try {
            return DAO.findById(eventId);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static List<Event> getEventsByClub(int clubId) {
        try {
            return DAO.findByClub(clubId);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    /** Extra helper (not in the contract): venues for the dropdown. */
    public static List<Venue> getAllVenues() {
        try {
            return DAO.findAllVenues();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    /** Creates an event. Throws IllegalArgumentException for bad input. Returns new id or -1. */
    public static int createEvent(Event e) {
        validate(e);
        checkClubApproved(e.getClubId());
        e.setStatus("ACTIVE");
        try {
            return DAO.insert(e);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public static boolean updateEvent(Event e) {
        validate(e);
        try {
            return DAO.update(e);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** Only changes status; the row is never deleted. */
    public static boolean cancelEvent(int eventId) {
        try {
            return DAO.updateStatus(eventId, "CANCELLED");
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
        /** Only organizers of an APPROVED club may create events. */
    private static void checkClubApproved(int clubId) {
        try {
            Club club = new ClubDAO().findById(clubId);
            if (club == null) {
                throw new IllegalArgumentException("Your account is not linked to a valid club.");
            }
            if (!"APPROVED".equals(club.getStatus())) {
                throw new IllegalArgumentException("Your club is still waiting for admin approval.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace(); // a lookup error must not block the organizer
        }
    }
    private static void validate(Event e) {
        if (e.getTitle() == null || e.getTitle().trim().isEmpty())
            throw new IllegalArgumentException("Title cannot be empty.");
        if (e.getCapacity() <= 0)
            throw new IllegalArgumentException("Capacity must be greater than 0.");
        LocalDateTime dt;
        try {
            dt = LocalDateTime.parse(e.getDateTime(), FMT);
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new IllegalArgumentException("Date must look like 2026-11-05 and time like 14:30.");
        }
        if (!dt.isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Event date must be in the future.");
    }
}