package com.mecca.model;

/** ActivityPoint model. Owner: M7. Replace/extend freely inside your own branch. */
public class ActivityPoint {
    private int apId;
    private int userId;
    private Integer eventId;
    private String ktuCode;
    private int level;
    private int points;
    private String status;
    private String awardedOn;
    private String certificateNote;

    public ActivityPoint() {}

    public int getApId() { return apId; }
    public void setApId(int apId) { this.apId = apId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }
    public String getKtuCode() { return ktuCode; }
    public void setKtuCode(String ktuCode) { this.ktuCode = ktuCode; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAwardedOn() { return awardedOn; }
    public void setAwardedOn(String awardedOn) { this.awardedOn = awardedOn; }
    public String getCertificateNote() { return certificateNote; }
    public void setCertificateNote(String certificateNote) { this.certificateNote = certificateNote; }
        // Display fields filled by JOIN queries (used by PointsPanel and ClaimsPanel)
    private String studentName;
    private String activityName;
    private int groupNo;

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }
    public int getGroupNo() { return groupNo; }
    public void setGroupNo(int groupNo) { this.groupNo = groupNo; }
}
