// src/main/java/com/mecca/model/RegistrantRow.java
package com.mecca.model;

/** One row shown in the organizer's attendance table: a registered student and whether they attended. */
public class RegistrantRow {
    private int regId;
    private String studentName;
    private String rollNumber;
    private boolean attended;

    public RegistrantRow() { }

    public RegistrantRow(int regId, String studentName, String rollNumber, boolean attended) {
        this.regId = regId;
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.attended = attended;
    }

    public int getRegId() { return regId; }
    public void setRegId(int regId) { this.regId = regId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public boolean isAttended() { return attended; }
    public void setAttended(boolean attended) { this.attended = attended; }
}