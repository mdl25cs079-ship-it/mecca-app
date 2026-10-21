package com.mecca.model;

/** Registration model. Owner: M5. Replace/extend freely inside your own branch. */
public class Registration {
    private int regId;
    private int userId;
    private int eventId;
    private String regDate;
    private String status;

    public Registration() {}

    public int getRegId() { return regId; }
    public void setRegId(int regId) { this.regId = regId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public String getRegDate() { return regDate; }
    public void setRegDate(String regDate) { this.regDate = regDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
