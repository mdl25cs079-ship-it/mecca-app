// src/main/java/com/mecca/model/Venue.java
package com.mecca.model;

/** A place where events are held. Maps to the venues table. */
public class Venue {
    private int venueId;
    private String name;
    private int capacityLimit;
    private String location;

    public Venue() { }

    public Venue(int venueId, String name, int capacityLimit, String location) {
        this.venueId = venueId;
        this.name = name;
        this.capacityLimit = capacityLimit;
        this.location = location;
    }

    public int getVenueId() { return venueId; }
    public void setVenueId(int venueId) { this.venueId = venueId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCapacityLimit() { return capacityLimit; }
    public void setCapacityLimit(int capacityLimit) { this.capacityLimit = capacityLimit; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    @Override
    public String toString() { return name + " (cap " + capacityLimit + ")"; }
}