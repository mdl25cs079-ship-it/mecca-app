package com.mecca.model;

/** KTUCategory model. Owner: M7. Replace/extend freely inside your own branch. */
public class KTUCategory {
    private String code;
    private int groupNo;
    private String name;
    private int maxPoints;
    private int[] pointsByLevel;

    public KTUCategory() {}

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public int getGroupNo() { return groupNo; }
    public void setGroupNo(int groupNo) { this.groupNo = groupNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getMaxPoints() { return maxPoints; }
    public void setMaxPoints(int maxPoints) { this.maxPoints = maxPoints; }
    public int[] getPointsByLevel() { return pointsByLevel; }
    public void setPointsByLevel(int[] pointsByLevel) { this.pointsByLevel = pointsByLevel; }

    @Override public String toString() { return code + " - " + name + " (Group " + (char) ('A' + groupNo - 1) + ")"; }
}
