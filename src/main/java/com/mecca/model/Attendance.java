// src/main/java/com/mecca/model/Attendance.java
package com.mecca.model;

import java.time.LocalDateTime;

/** One attendance record: a registered student who checked in to an event. */
public class Attendance {
    private int attId;
    private int regId;                 // links to registrations.reg_id
    private LocalDateTime checkInTime;
    private boolean verified;

    public Attendance() { }

    public Attendance(int attId, int regId, LocalDateTime checkInTime, boolean verified) {
        this.attId = attId;
        this.regId = regId;
        this.checkInTime = checkInTime;
        this.verified = verified;
    }

    public int getAttId() { return attId; }
    public void setAttId(int attId) { this.attId = attId; }

    public int getRegId() { return regId; }
    public void setRegId(int regId) { this.regId = regId; }

    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}