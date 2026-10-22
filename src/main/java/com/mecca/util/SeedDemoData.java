package com.mecca.util;

import com.mecca.db.DatabaseConnection;
import com.mecca.service.AuthService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        if (count(c, "users") > 0 || count(c, "events") > 0) {
            System.out.println("Seed skipped: tables are not empty");
            return;
        }
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String pw = AuthService.hashPassword("pass123");

        long c1 = ins(c, "INSERT INTO clubs(name, description, status) VALUES (?,?,?)", "Coding Club", "Tech events", "APPROVED");
        long c2 = ins(c, "INSERT INTO clubs(name, description, status) VALUES (?,?,?)", "Nature Club", "Green events", "APPROVED");
        long c3 = ins(c, "INSERT INTO clubs(name, description, status) VALUES (?,?,?)", "Arts Club", "Arts and culture", "APPROVED");

        long v1 = ins(c, "INSERT INTO venues(name, capacity_limit, location) VALUES (?,?,?)", "Main Auditorium", 300, "Admin Block");
        long v2 = ins(c, "INSERT INTO venues(name, capacity_limit, location) VALUES (?,?,?)", "Seminar Hall", 100, "CS Block");

        ins(c, "INSERT INTO users(name, email, password_hash, role) VALUES (?,?,?,?)", "Admin", "admin@mecca.test", pw, "ADMIN");
        ins(c, "INSERT INTO users(name, email, password_hash, role, managed_club_id) VALUES (?,?,?,?,?)", "Organizer One", "org1@mecca.test", pw, "ORGANIZER", c1);
        ins(c, "INSERT INTO users(name, email, password_hash, role, managed_club_id) VALUES (?,?,?,?,?)", "Organizer Two", "org2@mecca.test", pw, "ORGANIZER", c2);
        long[] st = new long[5];
        for (int i = 0; i < 5; i++) {
            st[i] = ins(c, "INSERT INTO users(name, email, password_hash, role, roll_number) VALUES (?,?,?,?,?)",
                    "Student " + (i + 1), "student" + (i + 1) + "@mecca.test", pw, "STUDENT", "MEC25CS0" + (10 + i));
        }

        String q = "INSERT INTO events(club_id, title, description, date_time, capacity, venue_id, ktu_code, level, status) VALUES (?,?,?,?,?,?,?,?,?)";
        long e1 = ins(c, q, c1, "Hackathon 2026", "24 hour coding event", "2026-11-05 10:00", 50, v2, "TEC", 2, "ACTIVE");
        long e2 = ins(c, q, c2, "Tree Plantation Drive", "Plant trees on campus", "2026-11-10 09:00", 100, v1, "NAT", 1, "ACTIVE");
        long e3 = ins(c, q, c3, "Cultural Fest", "Music, dance and drama", "2026-11-20 14:00", 300, v1, "ART", 3, "ACTIVE");
        long e4 = ins(c, q, c1, "Java Workshop", "Hands-on Java session", "2026-11-25 10:00", 2, v2, "TEC", 1, "ACTIVE");
        long e5 = ins(c, q, c1, "Tech Quiz", "Past quiz event", "2026-09-15 11:00", 60, v2, "TEC", 1, "COMPLETED");
        ins(c, q, c3, "Sports Meet", "Cancelled due to rain", "2026-12-01 09:00", 80, v1, "SPT", 2, "CANCELLED");

        String rq = "INSERT INTO registrations(user_id, event_id, reg_date) VALUES (?,?,?)";
        long r1 = ins(c, rq, st[0], e5, now);
        long r2 = ins(c, rq, st[1], e5, now);
        ins(c, rq, st[2], e5, now);
        ins(c, rq, st[0], e4, now);
        ins(c, rq, st[1], e4, now);
        ins(c, rq, st[2], e1, now);
        ins(c, rq, st[3], e2, now);
        ins(c, rq, st[4], e3, now);

        String aq = "INSERT INTO attendance(reg_id, check_in_time) VALUES (?,?)";
        ins(c, aq, r1, "2026-09-15 11:05");
        ins(c, aq, r2, "2026-09-15 11:10");

        System.out.println("Seed done. Logins: admin@mecca.test, org1@mecca.test, org2@mecca.test, student1..5@mecca.test, password pass123");
        System.out.println("Student user ids are 4 to 8. Event 1 is Hackathon 2026. Event 4 is full.");
    }

    private static int count(Connection c, String t) throws Exception {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT COUNT(*) FROM " + t)) {
            return r.next() ? r.getInt(1) : 0;
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
