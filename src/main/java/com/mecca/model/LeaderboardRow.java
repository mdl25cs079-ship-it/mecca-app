package com.mecca.model;

/** LeaderboardRow model. Owner: M3. Replace/extend freely inside your own branch. */
public class LeaderboardRow {
    private int rank;
    private String name;
    private String rollNumber;
    private int totalPoints;

    public LeaderboardRow() {}

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
}
