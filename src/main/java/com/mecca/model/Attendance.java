package com.mecca.model;

/** Attendance model. Owner: M6. Replace/extend freely inside your own branch. */
public class Attendance {
    private int attId;
    private int regId;
    private String checkInTime;
    private boolean verified;

    public Attendance() {}

    public int getAttId() { return attId; }
    public void setAttId(int attId) { this.attId = attId; }
    public int getRegId() { return regId; }
    public void setRegId(int regId) { this.regId = regId; }
    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}
