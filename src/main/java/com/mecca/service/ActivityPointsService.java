// src/main/java/com/mecca/service/ActivityPointsService.java
package com.mecca.service;

import com.mecca.dao.AttendanceDAO;
import com.mecca.dao.CategoryDAO;
import com.mecca.dao.PointsDAO;
import com.mecca.model.ActivityPoint;
import com.mecca.model.Event;
import com.mecca.model.KTUCategory;
import com.mecca.model.PointsSummary;
import com.mecca.util.Constants;

import java.util.Collections;
import java.util.List;

/**
 * Service implementing business logic for KTU Activity Points.
 * Owner: M7.
 * All service methods are public static.
 */
public class ActivityPointsService {

    private ActivityPointsService() { }

    /**
     * Awards KTU activity points to a student upon verified attendance at an event.
     * Enforces duplicate-award prevention, valid category mapping, category point caps,
     * and admission-type group caps.
     *
     * @param userId  The student ID.
     * @param eventId The event ID.
     */
    public static void awardPoints(int userId, int eventId) {
        if (userId <= 0 || eventId <= 0) {
            return;
        }

        // Validate user existence
        String admissionType = PointsDAO.getAdmissionType(userId);
        if (admissionType == null) {
            return;
        }

        // Validate verified attendance using AttendanceDAO
        if (!AttendanceDAO.hasAttended(userId, eventId)) {
            return;
        }

        // Prevent duplicate awards for the same user and event
        if (PointsDAO.hasAwardedEvent(userId, eventId)) {
            return;
        }

        new EventService();
        // Retrieve event details
        Event event = EventService.getEventById(eventId);
        if (event == null) {
            return;
        }

        String ktuCode = event.getKtuCode();
        if (ktuCode == null || ktuCode.isBlank()) {
            return;
        }

        // Resolve KTU category
        KTUCategory category = CategoryDAO.findByCode(ktuCode.trim());
        if (category == null) {
            System.err.println("Skipping points award: unsupported KTU category '" + ktuCode + "' for event " + eventId);
            return;
        }

        // Calculate base points for the event level (1 to 5)
        int level = Math.max(1, Math.min(5, event.getLevel()));
        int[] pointsByLevel = category.getPointsByLevel();
        int basePoints = (pointsByLevel != null && pointsByLevel.length >= level) ? pointsByLevel[level - 1] : 0;
        if (basePoints <= 0) {
            return;
        }

        // Apply category maximum cap
        int currentCatPoints = PointsDAO.sumApprovedPointsByKtuCode(userId, category.getCode());
        int remainingCat = Math.max(0, category.getMaxPoints() - currentCatPoints);
        int pointsToAward = Math.min(basePoints, remainingCat);

        // Apply group cap (maximum 40 points counted per group)
        int[] groupTotals = PointsDAO.sumApprovedPointsByGroup(userId);
        int groupIndex = category.getGroupNo() - 1;
        int currentGroupPoints = (groupIndex >= 0 && groupIndex < 3) ? groupTotals[groupIndex] : 0;
        int remainingGroup = Math.max(0, Constants.GROUP_CAP - currentGroupPoints);
        pointsToAward = Math.min(pointsToAward, remainingGroup);

        // Insert the approved award
        PointsDAO.insertAward(
                userId,
                eventId,
                category.getCode(),
                level,
                pointsToAward,
                Constants.POINTS_APPROVED,
                event.getTitle()
        );
    }

    /**
     * Calculates the KTU Activity Points progress summary for a student.
     * Earned points are summed per group from approved awards, capped at 40 per group.
     * Required points and completion status are determined by the student's admission type.
     *
     * @param userId The student ID.
     * @return PointsSummary data object, or an empty default summary if user is not found.
     */
    public static PointsSummary getSummary(int userId) {
        String admissionType = PointsDAO.getAdmissionType(userId);
        if (admissionType == null || admissionType.isBlank()) {
            admissionType = Constants.ADM_REGULAR;
        }

        int reqPerGroup = Constants.requiredPerGroup(admissionType);
        int totalRequired = Constants.requiredTotal(admissionType);

        int[] rawGroupTotals = PointsDAO.sumApprovedPointsByGroup(userId);
        int[] earned = new int[3];
        earned[0] = Math.min(rawGroupTotals[0], Constants.GROUP_CAP);
        earned[1] = Math.min(rawGroupTotals[1], Constants.GROUP_CAP);
        earned[2] = Math.min(rawGroupTotals[2], Constants.GROUP_CAP);

        int total = earned[0] + earned[1] + earned[2];
        boolean creditsComplete = (earned[0] >= reqPerGroup && earned[1] >= reqPerGroup && earned[2] >= reqPerGroup);

        PointsSummary summary = new PointsSummary();
        summary.setAdmissionType(admissionType);
        summary.setEarned(earned);
        summary.setRequired(new int[]{reqPerGroup, reqPerGroup, reqPerGroup});
        summary.setTotal(total);
        summary.setTotalRequired(totalRequired);
        summary.setCreditsComplete(creditsComplete);
        return summary;
    }

    /**
     * Retrieves the student's complete activity point history, newest first.
     *
     * @param userId The student ID.
     * @return List of ActivityPoint records with display fields populated.
     */
    public static List<ActivityPoint> getHistory(int userId) {
        if (userId <= 0) {
            return Collections.emptyList();
        }
        return PointsDAO.findHistoryByUserId(userId);
    }

    /**
     * Submits a manual KTU points claim on behalf of a student.
     * Creates a claim record with status PENDING and 0 initial points awaiting admin approval.
     *
     * @param userId  The student ID.
     * @param ktuCode The KTU category code.
     * @param level   The achievement level (1 to 5).
     * @param note    Justification or certificate description.
     * @return true if submitted successfully, false if validation fails.
     */
    public static boolean submitClaim(int userId, String ktuCode, int level, String note) {
        if (userId <= 0 || ktuCode == null || ktuCode.isBlank()) {
            return false;
        }
        if (level < 1 || level > 5) {
            return false;
        }

        // Validate user
        if (PointsDAO.getAdmissionType(userId) == null) {
            return false;
        }

        // Validate category
        KTUCategory category = CategoryDAO.findByCode(ktuCode.trim());
        if (category == null) {
            return false;
        }

        try {
            PointsDAO.insertAward(
                    userId,
                    null, // manual claims have null event_id
                    category.getCode(),
                    level,
                    0,    // 0 points until approved
                    Constants.POINTS_PENDING,
                    note != null ? note.trim() : null
            );
            return true;
        } catch (Exception ex) {
            System.err.println("Could not submit claim: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all pending claims for admin review, ordered oldest to newest.
     *
     * @return List of pending ActivityPoint claims with display fields populated.
     */
    public static List<ActivityPoint> getPendingClaims() {
        return PointsDAO.findPendingClaims();
    }

    /**
     * Approves a pending manual claim by its ID.
     * Revalidates category rules, applies category caps and group caps, and updates status to APPROVED.
     *
     * @param apId The claim ID.
     */
    public static void approveClaim(int apId) {
        if (apId <= 0) {
            return;
        }

        ActivityPoint claim = PointsDAO.findById(apId);
        if (claim == null || !Constants.POINTS_PENDING.equals(claim.getStatus())) {
            return;
        }

        KTUCategory category = CategoryDAO.findByCode(claim.getKtuCode());
        if (category == null) {
            System.err.println("Cannot approve claim " + apId + ": unknown KTU category " + claim.getKtuCode());
            return;
        }

        int level = Math.max(1, Math.min(5, claim.getLevel()));
        int[] pointsByLevel = category.getPointsByLevel();
        int basePoints = (pointsByLevel != null && pointsByLevel.length >= level) ? pointsByLevel[level - 1] : 0;

        // Apply category maximum cap
        int currentCatPoints = PointsDAO.sumApprovedPointsByKtuCode(claim.getUserId(), category.getCode());
        int remainingCat = Math.max(0, category.getMaxPoints() - currentCatPoints);
        int pointsToAward = Math.min(basePoints, remainingCat);

        // Apply group cap (40 points per group)
        int[] groupTotals = PointsDAO.sumApprovedPointsByGroup(claim.getUserId());
        int groupIndex = category.getGroupNo() - 1;
        int currentGroupPoints = (groupIndex >= 0 && groupIndex < 3) ? groupTotals[groupIndex] : 0;
        int remainingGroup = Math.max(0, Constants.GROUP_CAP - currentGroupPoints);
        pointsToAward = Math.min(pointsToAward, remainingGroup);

        PointsDAO.updateClaimStatusAndPoints(apId, pointsToAward, Constants.POINTS_APPROVED);
    }

    /**
     * Rejects a pending claim by deleting the pending record from the database.
     *
     * @param apId The claim ID.
     */
    public static void rejectClaim(int apId) {
        if (apId <= 0) {
            return;
        }
        PointsDAO.deleteClaim(apId);
    }

    /**
     * Retrieves all available KTU activity categories.
     *
     * @return List of all KTUCategory objects.
     */
    public static List<KTUCategory> getCategories() {
        return CategoryDAO.findAll();
    }
}
