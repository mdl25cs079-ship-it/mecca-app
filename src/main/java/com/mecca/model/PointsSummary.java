package com.mecca.model;

/** PointsSummary model. Owner: M7. Replace/extend freely inside your own branch. */
public class PointsSummary {
    private String admissionType;
    private int[] earned;
    private int[] required;
    private int total;
    private int totalRequired;
    private boolean creditsComplete;

    public PointsSummary() {}

    public String getAdmissionType() { return admissionType; }
    public void setAdmissionType(String admissionType) { this.admissionType = admissionType; }
    public int[] getEarned() { return earned; }
    public void setEarned(int[] earned) { this.earned = earned; }
    public int[] getRequired() { return required; }
    public void setRequired(int[] required) { this.required = required; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getTotalRequired() { return totalRequired; }
    public void setTotalRequired(int totalRequired) { this.totalRequired = totalRequired; }
    public boolean isCreditsComplete() { return creditsComplete; }
    public void setCreditsComplete(boolean creditsComplete) { this.creditsComplete = creditsComplete; }
}
