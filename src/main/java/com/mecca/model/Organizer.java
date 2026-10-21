package com.mecca.model;

/** Club organizer user. Owner: M2. */
public class Organizer extends User {
    private int managedClubId;

    public int getManagedClubId() { return managedClubId; }
    public void setManagedClubId(int managedClubId) { this.managedClubId = managedClubId; }

    @Override public void openDashboard() { /* TODO M2 */ }
}
