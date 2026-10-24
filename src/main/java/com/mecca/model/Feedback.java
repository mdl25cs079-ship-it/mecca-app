// src/main/java/com/mecca/model/Feedback.java
package com.mecca.model;

/** One feedback entry: a student's rating and comments for an event. */
public class Feedback {
    private int feedbackId;
    private int eventId;
    private int userId;
    private int rating;        // 1 to 5
    private String comments;

    public Feedback() { }

    public Feedback(int feedbackId, int eventId, int userId, int rating, String comments) {
        this.feedbackId = feedbackId;
        this.eventId = eventId;
        this.userId = userId;
        this.rating = rating;
        this.comments = comments;
    }

    public int getFeedbackId() { return feedbackId; }
    public void setFeedbackId(int feedbackId) { this.feedbackId = feedbackId; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}