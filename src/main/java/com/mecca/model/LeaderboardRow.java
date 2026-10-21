// src/main/java/com/mecca/model/LeaderboardRow.java
package com.mecca.model;

/** One ranked line of the leaderboard. Owner: M3. */
public class LeaderboardRow {
    private int rank;
    private int userId;        // extra field: lets the panel highlight the logged-in student
    private String name;
    private String rollNumber;
    private int totalPoints;

    public LeaderboardRow(int rank, int userId, String name, String rollNumber, int totalPoints) {
        this.rank = rank;
        this.userId = userId;
        this.name = name;
        this.rollNumber = rollNumber;
        this.totalPoints = totalPoints;
    }

    public int getRank() { return rank; }
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getRollNumber() { return rollNumber; }
    public int getTotalPoints() { return totalPoints; }
}