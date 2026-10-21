package com.mecca.model;

/** Event model. Owner: M4. Replace/extend freely inside your own branch. */
public class Event {
    private int eventId;
    private int clubId;
    private String title;
    private String description;
    private String dateTime;
    private int capacity;
    private int venueId;
    private String ktuCode;
    private int level;
    private String status;

    public Event() {}

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public int getClubId() { return clubId; }
    public void setClubId(int clubId) { this.clubId = clubId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public int getVenueId() { return venueId; }
    public void setVenueId(int venueId) { this.venueId = venueId; }
    public String getKtuCode() { return ktuCode; }
    public void setKtuCode(String ktuCode) { this.ktuCode = ktuCode; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override public String toString() { return title; }
}
