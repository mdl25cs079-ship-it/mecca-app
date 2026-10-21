// src/main/java/com/mecca/model/Admin.java
package com.mecca.model;

import com.mecca.ui.MainFrame;

/** Admin user. Owner: M2. */
public class Admin extends User {

    /** Opens the main window; MainFrame shows the admin tabs. */
    @Override public void openDashboard() { new MainFrame().setVisible(true); }
}