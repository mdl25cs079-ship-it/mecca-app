package com.mecca.model;

import java.time.LocalDateTime;

/**
 * A student's registration for an event.
 * Maps to the Registration table: regId, userId, eventId, regDate, status.
 */
public class Registration {

    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private int regId;
    private int userId;
    private int eventId;
    private LocalDateTime regDate;
    private String status;

    public Registration() {
    }

    public Registration(int userId, int eventId) {
        this.userId = userId;
        this.eventId = eventId;
        this.regDate = LocalDateTime.now();
        this.status = STATUS_CONFIRMED;
    }

    public Registration(int regId, int userId, int eventId, LocalDateTime regDate, String status) {
        this.regId = regId;
        this.userId = userId;
        this.eventId = eventId;
        this.regDate = regDate;
        this.status = status;
    }

    public int getRegId() {
        return regId;
    }

    public void setRegId(int regId) {
        this.regId = regId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public LocalDateTime getRegDate() {
        return regDate;
    }

    public void setRegDate(LocalDateTime regDate) {
        this.regDate = regDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Registration{regId=" + regId + ", userId=" + userId + ", eventId=" + eventId
                + ", regDate=" + regDate + ", status='" + status + "'}";
    }
}
