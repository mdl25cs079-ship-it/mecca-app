// src/main/java/com/mecca/service/EventService.java
package com.mecca.service;

import com.mecca.dao.EventDAO;
import com.mecca.model.Event;
import com.mecca.model.Venue;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/** Business logic for events. */
public class EventService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final EventDAO dao = new EventDAO();

    public static List<Event> getActiveEvents() {
        try {
            return dao.findActive();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static Event getEventById(int eventId) {
        try {
            return dao.findById(eventId);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static List<Event> getEventsByClub(int clubId) {
        try {
            return dao.findByClub(clubId);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    /** Extra helper (not in the contract): venues for the dropdown. */
    public static List<Venue> getAllVenues() {
        try {
            return dao.findAllVenues();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    /** Creates an event. Throws IllegalArgumentException for bad input. Returns new id or -1. */
    public static int createEvent(Event e) {
        validate(e);
        e.setStatus("ACTIVE");
        try {
            return dao.insert(e);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public static boolean updateEvent(Event e) {
        validate(e);
        try {
            return dao.update(e);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** Only changes status; the row is never deleted. */
    public static  boolean cancelEvent(int eventId) {
        try {
            return dao.updateStatus(eventId, "CANCELLED");
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
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