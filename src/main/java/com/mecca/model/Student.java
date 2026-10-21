// src/main/java/com/mecca/model/Student.java
package com.mecca.model;

import com.mecca.ui.MainFrame;

/** Student user. Owner: M2. */
public class Student extends User {
    private String rollNumber;
    private String admissionType = "REGULAR";
    private int cumulativeKtuPoints;

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public String getAdmissionType() { return admissionType; }
    public void setAdmissionType(String admissionType) { this.admissionType = admissionType; }
    public int getCumulativeKtuPoints() { return cumulativeKtuPoints; }
    public void setCumulativeKtuPoints(int p) { this.cumulativeKtuPoints = p; }

    /** Opens the main window; MainFrame shows the student tabs. */
    @Override public void openDashboard() { new MainFrame().setVisible(true); }
}