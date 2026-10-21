package com.mecca.model;

/** RegistrantRow model. Owner: M6. Replace/extend freely inside your own branch. */
public class RegistrantRow {
    private int regId;
    private int userId;
    private String studentName;
    private String rollNumber;
    private boolean attended;

    public RegistrantRow() {}

    public int getRegId() { return regId; }
    public void setRegId(int regId) { this.regId = regId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public boolean isAttended() { return attended; }
    public void setAttended(boolean attended) { this.attended = attended; }
}
