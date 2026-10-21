// src/main/java/com/mecca/model/Organizer.java
package com.mecca.model;

import com.mecca.ui.MainFrame;

/** Club organizer user. Owner: M2. */
public class Organizer extends User {
    private int managedClubId;   // 0 means no club assigned

    public int getManagedClubId() { return managedClubId; }
    public void setManagedClubId(int managedClubId) { this.managedClubId = managedClubId; }

    /** Opens the main window; MainFrame shows the organizer tabs. */
    @Override public void openDashboard() { new MainFrame().setVisible(true); }
}