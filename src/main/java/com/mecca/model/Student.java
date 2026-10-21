package com.mecca.model;

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

    @Override public void openDashboard() { /* TODO M2 */ }
}
