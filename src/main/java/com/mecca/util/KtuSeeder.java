// src/main/java/com/mecca/util/KtuSeeder.java
package com.mecca.util;

import com.mecca.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Seeds KTU activity point categories into the ktu_categories table if empty.
 * Owner: M7.
 *
 * Categories are derived from Table 2 of the official APJ Abdul Kalam Technological University
 * Activity Points Handbook (2024 Scheme). Discrepancies between the handbook and the team specification
 * (specifically codes 2.21 and 2.23) are explicitly separated and preserved for full compatibility.
 */
public final class KtuSeeder {

    public static final class CategoryEntry {
        private final String code;
        private final int groupNo;
        private final String name;
        private final int maxPoints;
        private final int[] pointsByLevel;
        private final boolean handbookVerified;
        private final String note;

        public CategoryEntry(String code, int groupNo, String name, int maxPoints, int[] pointsByLevel,
                             boolean handbookVerified, String note) {
            this.code = code;
            this.groupNo = groupNo;
            this.name = name;
            this.maxPoints = maxPoints;
            this.pointsByLevel = pointsByLevel;
            this.handbookVerified = handbookVerified;
            this.note = note;
        }

        public String getCode() { return code; }
        public int getGroupNo() { return groupNo; }
        public String getName() { return name; }
        public int getMaxPoints() { return maxPoints; }
        public int[] getPointsByLevel() { return pointsByLevel; }
        public boolean isHandbookVerified() { return handbookVerified; }
        public String getNote() { return note; }
    }

    /** Verified categories that match Table 2 of the 2024 KTU Activity Points Handbook. */
    private static final List<CategoryEntry> VERIFIED_ENTRIES = List.of(
            // Group 1: Sports, Arts, Community, Health & Safety, Union/Clubs
            new CategoryEntry("1.1", 1, "Sports/Games/Arts (Participation)", 40,
                    new int[]{1, 5, 10, 20, 40}, true, "Handbook Table 2, p. 6"),
            new CategoryEntry("1.2", 1, "Sports/Games/Arts (Winners) - Single Events", 40,
                    new int[]{5, 10, 20, 40, 40}, true, "Handbook Table 2, p. 6"),
            new CategoryEntry("1.3", 1, "Sports/Games/Arts (Winners) - Group Events", 40,
                    new int[]{3, 5, 15, 30, 40}, true, "Handbook Table 2, p. 6"),
            new CategoryEntry("1.4", 1, "Publication in College Magazine", 20,
                    new int[]{5, 5, 5, 5, 5}, true, "Handbook Table 2, p. 6; fixed 5 pts per magazine, max 20"),
            new CategoryEntry("1.6", 1, "Community Service & Allied Activities (Two Days)", 10,
                    new int[]{5, 5, 5, 5, 5}, true, "Handbook Table 2, p. 7; fixed 5 pts, max 10"),
            new CategoryEntry("1.7", 1, "Community Service & Allied Activities (Up to One Week)", 10,
                    new int[]{10, 10, 10, 10, 10}, true, "Handbook Table 2, p. 7; fixed 10 pts, max 10"),
            new CategoryEntry("1.8", 1, "Blood donation", 10,
                    new int[]{5, 5, 5, 5, 5}, true, "Handbook Table 2, p. 7; fixed 5 pts per donation, max 10"),
            new CategoryEntry("1.20", 1, "Emergency Response, First Aid, CPR & Fire Safety", 10,
                    new int[]{5, 5, 5, 5, 5}, true, "Handbook Table 2, p. 9; fixed 5 pts per training, max 10"),
            new CategoryEntry("1.24", 1, "Hobby Clubs under College Union", 10,
                    new int[]{5, 5, 5, 5, 5}, true, "Official Handbook Table 2, p. 10; 5 pts per year, max 10"),

            // Group 2: Technical Events, Competitions, Workshops, Internships
            new CategoryEntry("2.1", 2, "Tech-Fest-Participation", 40,
                    new int[]{2, 5, 10, 20, 30}, true, "Handbook Table 2, p. 11"),
            new CategoryEntry("2.2", 2, "Tech-Fest-Winners", 40,
                    new int[]{5, 10, 20, 40, 40}, true, "Handbook Table 2, p. 11"),
            new CategoryEntry("2.3", 2, "Professional Society Event - Participation", 40,
                    new int[]{2, 5, 10, 15, 20}, true, "Handbook Table 2, p. 11"),
            new CategoryEntry("2.4", 2, "Professional Society Event - Winners", 40,
                    new int[]{3, 7, 15, 25, 35}, true, "Handbook Table 2, p. 11"),
            new CategoryEntry("2.5", 2, "Workshop/Seminar at IIT/NIT/NIRF Top 100", 15,
                    new int[]{5, 5, 5, 5, 5}, true, "Handbook Table 2, p. 11; fixed 5 pts per event, max 15"),
            new CategoryEntry("2.20", 2, "Short-Term Internship / Clinical Exposure", 10,
                    new int[]{10, 10, 10, 10, 10}, true, "Official Handbook Table 2, p. 14; 10 pts, max 10"),

            // Group 3: Industry Exposure, Long-Term Internship, Hackathons, Skilling
            new CategoryEntry("3.1", 3, "Industrial Visit Report", 20,
                    new int[]{5, 5, 5, 5, 5}, true, "Handbook Table 2, p. 16; fixed 5 pts, max 20"),
            new CategoryEntry("3.3", 3, "Long-Term Internship (3.5 Months)", 20,
                    new int[]{15, 15, 15, 15, 15}, true, "Handbook Table 2, p. 17; fixed 15 pts, max 20"),
            new CategoryEntry("3.15", 3, "National Hackathon (Prize)", 40,
                    new int[]{30, 30, 30, 40, 40}, true, "Handbook Table 2, p. 20; max 40 (1st: 40, 2nd: 35, 3rd: 30)"),
            new CategoryEntry("3.17", 3, "Approved Skilling Course (Per Hour)", 40,
                    new int[]{1, 1, 1, 1, 1}, true, "Handbook Table 2, p. 20; 1 pt per hour, max 40")
    );

    /**
     * Categories from the MECCA team specification with documented discrepancies against the 2024 handbook.
     * Retained for backward compatibility with existing team contracts and tests.
     */
    private static final List<CategoryEntry> TEAM_SPEC_COMPATIBILITY_ENTRIES = List.of(
            new CategoryEntry("2.21", 2, "Hobby club executive/convenor", 10,
                    new int[]{5, 5, 5, 5, 5}, false,
                    "Discrepancy: Team spec assigned code 2.21 under Group II. Official 2024 Handbook Table 2 places Hobby Clubs under 1.24 in Group I. 2.21 in Handbook is English Proficiency Certifications."),
            new CategoryEntry("2.23", 2, "Short-term internship", 10,
                    new int[]{10, 10, 10, 10, 10}, false,
                    "Discrepancy: Code 2.23 does not exist in 2024 Handbook Group II. Official code for Short-Term Internship is 2.20.")
    );

    private KtuSeeder() { }

    /** Returns unmodifiable list of verified handbook categories. */
    public static List<CategoryEntry> getVerifiedEntries() {
        return Collections.unmodifiableList(VERIFIED_ENTRIES);
    }

    /** Returns unmodifiable list of team-specification compatibility categories. */
    public static List<CategoryEntry> getCompatibilityEntries() {
        return Collections.unmodifiableList(TEAM_SPEC_COMPATIBILITY_ENTRIES);
    }

    /** Returns all categories to seed (verified + compatibility). */
    public static List<CategoryEntry> getAllSeedEntries() {
        List<CategoryEntry> all = new ArrayList<>(VERIFIED_ENTRIES.size() + TEAM_SPEC_COMPATIBILITY_ENTRIES.size());
        all.addAll(VERIFIED_ENTRIES);
        all.addAll(TEAM_SPEC_COMPATIBILITY_ENTRIES);
        return Collections.unmodifiableList(all);
    }

    /**
     * Seeds the ktu_categories table if it is currently empty.
     * Safe to run on every start; returns immediately if any rows exist.
     */
    public static void seedIfEmpty() {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM ktu_categories")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not check ktu_categories table: " + e.getMessage(), e);
        }

        String sql = "INSERT INTO ktu_categories (code, group_no, name, max_points, "
                   + "points_l1, points_l2, points_l3, points_l4, points_l5) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        boolean originalAutoCommit;
        try {
            originalAutoCommit = conn.getAutoCommit();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not inspect database auto-commit mode: " + e.getMessage(), e);
        }

        List<CategoryEntry> toSeed = getAllSeedEntries();
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (CategoryEntry entry : toSeed) {
                    ps.setString(1, entry.getCode());
                    ps.setInt(2, entry.getGroupNo());
                    ps.setString(3, entry.getName());
                    ps.setInt(4, entry.getMaxPoints());
                    ps.setInt(5, entry.getPointsByLevel()[0]);
                    ps.setInt(6, entry.getPointsByLevel()[1]);
                    ps.setInt(7, entry.getPointsByLevel()[2]);
                    ps.setInt(8, entry.getPointsByLevel()[3]);
                    ps.setInt(9, entry.getPointsByLevel()[4]);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                // suppress secondary rollback exception to report original failure
            }
            throw new IllegalStateException("Could not seed ktu_categories: " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(originalAutoCommit);
            } catch (SQLException restoreEx) {
                // ignore auto-commit restoration error if already closed
            }
        }
    }
}
