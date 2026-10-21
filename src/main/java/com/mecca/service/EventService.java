package com.mecca.service;

import com.mecca.model.Event;

import java.util.ArrayList;
import java.util.List;

/** STUB. Owner: M4. Keep method names/signatures EXACTLY. All methods are static. */
public class EventService {
    public static List<Event> getActiveEvents() { return new ArrayList<>(); } // TODO M4
    public static Event getEventById(int eventId) { return null; }            // TODO M4
    public static int createEvent(Event e) { return -1; }                      // TODO M4
    public static boolean updateEvent(Event e) { return false; }               // TODO M4
    public static boolean cancelEvent(int eventId) { return false; }           // TODO M4
    public static List<Event> getEventsByClub(int clubId) { return new ArrayList<>(); } // TODO M4
}
