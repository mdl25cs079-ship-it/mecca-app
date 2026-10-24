// src/main/java/com/mecca/util/KtuSeeder.java
package com.mecca.util;

import com.mecca.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

/**
 * Keeps the ktu_categories reference table in step with Table 2 of the APJ Abdul Kalam Technological
 * University Activity Points Handbook (2024 Scheme). Owner: M7 (corrected by M1).
 *
 * The first digit of every code is its group: 1.x = Group A, 2.x = Group B, 3.x = Group C.
 * The table is refreshed with INSERT OR REPLACE once per run, so an older database that was seeded with
 * wrong codes is repaired automatically. This is reference data (like a country list), not demo data.
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

    private static CategoryEntry e(String code, int group, String name, int max, int... byLevel) {
        return new CategoryEntry(code, group, name, max, byLevel, true, "Handbook Table 2");
    }

    private static final int[] FLAT_5 = {5, 5, 5, 5, 5};

    /** Group A (1.x), Group B (2.x), Group C (3.x). Points are listed for Level 1..5. */
    private static final List<CategoryEntry> VERIFIED_ENTRIES = List.of(
            // Group A: sports, arts, community, health and life skills, NSS/NCC
            e("1.1", 1, "Sports/Games/Arts (Participation)", 40, 1, 5, 10, 20, 40),
            e("1.2", 1, "Sports/Games/Arts (Winners) - Single Events", 40, 5, 10, 20, 40, 40),
            e("1.3", 1, "Sports/Games/Arts (Winners) - Group Events", 40, 3, 5, 15, 30, 40),
            e("1.4", 1, "Publication in College Magazine", 20, 5, 5, 5, 5, 5),
            e("1.5", 1, "Four-wheeler license", 5, 5, 5, 5, 5, 5),
            e("1.6", 1, "Community Service & Allied Activities (Two Days)", 10, 5, 5, 5, 5, 5),
            e("1.7", 1, "Community Service & Allied Activities (Up to One Week)", 10, 10, 10, 10, 10, 10),
            e("1.8", 1, "Blood donation", 10, 5, 5, 5, 5, 5),
            e("1.9", 1, "THRIVE Project (per semester)", 20, 10, 10, 10, 10, 10),
            e("1.10", 1, "Tree planting", 5, 5, 5, 5, 5, 5),
            e("1.11", 1, "NSS Volunteer (two years)", 40, 30, 30, 30, 30, 30),
            e("1.20", 1, "Emergency Response, First Aid, CPR & Fire Safety Training", 10, 5, 5, 5, 5, 5),
            e("1.21", 1, "Basic Swimming", 10, 5, 5, 5, 5, 5),

            // Group B: technical events, presentations, leadership, clubs, internships
            e("2.1", 2, "Tech Fest - Participation", 40, 2, 5, 10, 20, 30),
            e("2.2", 2, "Tech Fest - Winners", 40, 5, 10, 20, 40, 40),
            e("2.3", 2, "Professional Society Event - Participation", 40, 2, 5, 10, 15, 20),
            e("2.4", 2, "Professional Society Event - Winners", 40, 3, 7, 15, 25, 35),
            e("2.5", 2, "Workshop/Seminar at IIT/NIT/NIRF Top 100", 15, 5, 5, 5, 5, 5),
            e("2.7", 2, "Paper Presentation - Participation (KTU/IIT/NIT events)", 40, 10, 10, 10, 10, 10),
            e("2.9", 2, "Paper Presentation - Participation (affiliated institutions)", 40, 5, 5, 5, 5, 5),
            e("2.20", 2, "College Magazine Editorial Board", 10, 5, 5, 5, 5, 5),
            e("2.21", 2, "Hobby Clubs under College Union", 10, 5, 5, 5, 5, 5),
            e("2.23", 2, "Short-Term Internship / Clinical Exposure Training", 10, 10, 10, 10, 10, 10),

            // Group C: industry exposure, projects, internships, hackathons, skilling
            e("3.1", 3, "Industrial Visit / Industrial Training Report", 20, 5, 5, 5, 5, 5),
            e("3.2", 3, "Best Mini Project / Best Project / Best Seminar", 20, 5, 5, 5, 5, 5),
            e("3.3", 3, "Long-Term Internship (3.5 months)", 20, 15, 15, 15, 15, 15),
            e("3.15", 3, "National Hackathon (Prize)", 40, 30, 30, 30, 40, 40),
            e("3.17", 3, "Approved Skilling Course (per hour)", 40, 1, 1, 1, 1, 1)
    );

    /** Codes that earlier versions seeded wrongly. They are removed if nothing refers to them. */
    private static final String[] STALE_CODES = {"1.24"};

    private static boolean done = false;

    private KtuSeeder() { }

    public static List<CategoryEntry> getVerifiedEntries() {
        return Collections.unmodifiableList(VERIFIED_ENTRIES);
    }

    /** Kept for older callers. Nothing is kept for compatibility any more, so this is empty. */
    public static List<CategoryEntry> getCompatibilityEntries() {
        return Collections.emptyList();
    }

    public static List<CategoryEntry> getAllSeedEntries() {
        return getVerifiedEntries();
    }

    /** Refreshes the category table once per run. Safe to call as often as you like. Name kept for old callers. */
    public static synchronized void seedIfEmpty() {
        if (done) {
            return;
        }
        Connection conn = DatabaseConnection.getInstance().getConnection();
        String sql = "INSERT OR REPLACE INTO ktu_categories (code, group_no, name, max_points, "
                   + "points_l1, points_l2, points_l3, points_l4, points_l5) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (CategoryEntry entry : VERIFIED_ENTRIES) {
                    ps.setString(1, entry.getCode());
                    ps.setInt(2, entry.getGroupNo());
                    ps.setString(3, entry.getName());
                    ps.setInt(4, entry.getMaxPoints());
                    for (int i = 0; i < 5; i++) {
                        ps.setInt(5 + i, entry.getPointsByLevel()[i]);
                    }
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            for (String stale : STALE_CODES) {
                try (PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM ktu_categories WHERE code = ? "
                      + "AND NOT EXISTS (SELECT 1 FROM activity_points WHERE ktu_code = ?) "
                      + "AND NOT EXISTS (SELECT 1 FROM events WHERE ktu_code = ?)")) {
                    del.setString(1, stale);
                    del.setString(2, stale);
                    del.setString(3, stale);
                    del.executeUpdate();
                }
            }
            conn.commit();
            done = true;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
                // report the original failure below
            }
            throw new IllegalStateException("Could not seed ktu_categories: " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(originalAutoCommit);
            } catch (SQLException ignored) {
                // connection already closed
            }
        }
    }
}