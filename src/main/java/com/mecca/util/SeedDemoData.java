// src/main/java/com/mecca/util/SeedDemoData.java
package com.mecca.util;

import com.mecca.db.DatabaseConnection;
import com.mecca.service.ActivityPointsService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Demo data: 3 clubs, 2 venues, 6 events, some registrations and attendances.
 * It does NOT create users. SeedUsers already creates them (password mecca123).
 * Everything runs in one transaction, so a failure never leaves half the data behind.
 */
public class SeedDemoData {

    public static void main(String[] args) {
        try {
            run();
        } catch (Throwable x) {
            System.out.println("Seed failed: " + x);
        }
    }

    public static void run() throws Exception {
        Connection c = DatabaseConnection.getInstance().getConnection();
        if (count(c, "clubs") > 0 || count(c, "events") > 0) {
            System.out.println("Seed skipped: clubs or events already exist");
            return;
        }
        if (count(c, "users") == 0) {
            throw new IllegalStateException("No users yet. Run Main once first so SeedUsers creates them.");
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        long userA = 0, userB = 0, quizEvent = 0; // used after commit to award points

        boolean oldAutoCommit = c.getAutoCommit();
        try {
            c.setAutoCommit(false);

            long c1 = ins(c, "INSERT INTO clubs(name, description, status) VALUES (?,?,?)", "Coding Club", "Tech events", "APPROVED");
            long c2 = ins(c, "INSERT INTO clubs(name, description, status) VALUES (?,?,?)", "Nature Club", "Green events", "APPROVED");
            long c3 = ins(c, "INSERT INTO clubs(name, description, status) VALUES (?,?,?)", "Arts Club", "Arts and culture", "APPROVED");

            long v1 = ins(c, "INSERT INTO venues(name, capacity_limit, location) VALUES (?,?,?)", "Main Auditorium", 300, "Admin Block");
            long v2 = ins(c, "INSERT INTO venues(name, capacity_limit, location) VALUES (?,?,?)", "Seminar Hall", 100, "CS Block");

            // Link the existing organizers to a club, so My Events and Create Event work
            exec(c, "UPDATE users SET managed_club_id = ? WHERE email = ?", c1, "organizer1@mecca.test");
            exec(c, "UPDATE users SET managed_club_id = ? WHERE email = ?", c2, "organizer2@mecca.test");

            // Look up the students that SeedUsers created
            long[] st = new long[5];
            for (int i = 0; i < 5; i++) {
                st[i] = userId(c, "student" + (i + 1) + "@mecca.test");
            }

            // KTU codes below exist in the ktu_categories seed table (1.1, 1.6, 2.1)
            String q = "INSERT INTO events(club_id, title, description, date_time, capacity, venue_id, ktu_code, level, status) VALUES (?,?,?,?,?,?,?,?,?)";
            long e1 = ins(c, q, c1, "Hackathon 2026", "24 hour coding event", "2026-11-05 10:00", 50, v2, "2.1", 2, "ACTIVE");
            long e2 = ins(c, q, c2, "Tree Plantation Drive", "Plant trees on campus", "2026-11-10 09:00", 100, v1, "1.6", 1, "ACTIVE");
            long e3 = ins(c, q, c3, "Cultural Fest", "Music, dance and drama", "2026-11-20 14:00", 300, v1, "1.1", 3, "ACTIVE");
            long e4 = ins(c, q, c1, "Java Workshop", "Hands-on Java session", "2026-11-25 10:00", 2, v2, "2.1", 1, "ACTIVE");
            long e5 = ins(c, q, c1, "Tech Quiz", "Past quiz event", "2026-09-15 11:00", 60, v2, "2.1", 1, "COMPLETED");
            ins(c, q, c3, "Sports Meet", "Cancelled due to rain", "2026-12-01 09:00", 80, v1, "1.1", 2, "CANCELLED");

            String rq = "INSERT INTO registrations(user_id, event_id, reg_date, status) VALUES (?,?,?,'CONFIRMED')";
            long r1 = ins(c, rq, st[0], e5, now);
            long r2 = ins(c, rq, st[1], e5, now);
            ins(c, rq, st[2], e5, now);
            ins(c, rq, st[0], e4, now);   // Java Workshop has capacity 2, so these two fill it
            ins(c, rq, st[1], e4, now);
            ins(c, rq, st[2], e1, now);
            ins(c, rq, st[3], e2, now);
            ins(c, rq, st[4], e3, now);

            String aq = "INSERT INTO attendance(reg_id, check_in_time, verified) VALUES (?,?,1)";
            ins(c, aq, r1, "2026-09-15 11:05");
            ins(c, aq, r2, "2026-09-15 11:10");

            userA = st[0];
            userB = st[1];
            quizEvent = e5;

            c.commit();
        } catch (Exception ex) {
            c.rollback();
            throw ex;
        } finally {
            c.setAutoCommit(oldAutoCommit);
        }

        // Give the two attendees their KTU points (after the commit, so a points error cannot undo the seed)
        try {
            ActivityPointsService.awardPoints((int) userA, (int) quizEvent);
            ActivityPointsService.awardPoints((int) userB, (int) quizEvent);
        } catch (Throwable t) {
            System.out.println("Points not awarded: " + t);
        }

        System.out.println("Seed done. 3 clubs, 6 events, 8 registrations, 2 attendances.");
        System.out.println("Logins use password mecca123: admin@mecca.test, organizer1@mecca.test, student1@mecca.test");
        System.out.println("Hackathon 2026 is open for student1. Java Workshop is full.");
    }

    private static int count(Connection c, String t) throws Exception {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT COUNT(*) FROM " + t)) {
            return r.next() ? r.getInt(1) : 0;
        }
    }

    private static long userId(Connection c, String email) throws Exception {
        try (PreparedStatement p = c.prepareStatement("SELECT user_id FROM users WHERE email = ?")) {
            p.setString(1, email);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) return r.getLong(1);
            }
        }
        throw new IllegalStateException("User not found: " + email + ". Run Main once first so SeedUsers creates the users.");
    }

    private static void exec(Connection c, String sql, Object... a) throws Exception {
        try (PreparedStatement p = c.prepareStatement(sql)) {
            for (int i = 0; i < a.length; i++) p.setObject(i + 1, a[i]);
            p.executeUpdate();
        }
    }

    private static long ins(Connection c, String sql, Object... a) throws Exception {
        try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < a.length; i++) p.setObject(i + 1, a[i]);
            p.executeUpdate();
            try (ResultSet k = p.getGeneratedKeys()) {
                return k.next() ? k.getLong(1) : -1;
            }
        }
    }
}