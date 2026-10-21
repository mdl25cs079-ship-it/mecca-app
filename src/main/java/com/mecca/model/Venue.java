package com.mecca.model;

/** Venue model. Owner: M4. Replace/extend freely inside your own branch. */
public class Venue {
    private int venueId;
    private String name;
    private int capacityLimit;
    private String location;

    public Venue() {}

    public int getVenueId() { return venueId; }
    public void setVenueId(int venueId) { this.venueId = venueId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCapacityLimit() { return capacityLimit; }
    public void setCapacityLimit(int capacityLimit) { this.capacityLimit = capacityLimit; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    @Override public String toString() { return name; }
}
