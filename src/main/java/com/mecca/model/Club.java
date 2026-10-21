package com.mecca.model;

/** Club model. Owner: M4. Replace/extend freely inside your own branch. */
public class Club {
    private int clubId;
    private String name;
    private String description;
    private String status;
    private double engagementScore;

    public Club() {}

    public int getClubId() { return clubId; }
    public void setClubId(int clubId) { this.clubId = clubId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getEngagementScore() { return engagementScore; }
    public void setEngagementScore(double engagementScore) { this.engagementScore = engagementScore; }

    @Override public String toString() { return name; }
}
