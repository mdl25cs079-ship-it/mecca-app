// src/main/java/com/mecca/util/SeedVenues.java
package com.mecca.util;

import com.mecca.db.DatabaseConnection;
import java.sql.*;

/** Makes sure the campus venues exist. Safe to call many times. */
public class SeedVenues {

    // {name, capacity, location}  <-- change capacities/locations to the real ones
    private static final String[][] VENUES = {
        {"SDPK Hall",            "600",  "Main Campus"},
        {"Internal Auditorium",  "400",  "Main Campus"},
        {"External Auditorium",  "1000", "Main Campus"},
        {"Medical Hall",         "200",  "Medical Block"},
        {"CCC",                  "120",  "Main Campus"},
        {"CCF",                  "120",  "Main Campus"},
        {"Seminar Hall",         "150",  "Admin Block"},
        {"Computer Lab 1",       "60",   "CS Department"},
        {"Computer Lab 2",       "60",   "CS Department"}
    };

    // Old defaults that are removed if no event is using them
    private static final String[] OLD = {"Main Auditorium", "Open Air Stage", "Sports Ground"};

    /** Name kept the same so existing callers still work. */
    public static void seedIfEmpty() {
        Connection con = DatabaseConnection.getInstance().getConnection();
        try {
            for (String old : OLD) {
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM venues WHERE name = ? AND NOT EXISTS " +
                        "(SELECT 1 FROM events e WHERE e.venue_id = venues.venue_id)")) {
                    ps.setString(1, old);
                    ps.executeUpdate();
                }
            }
            for (String[] v : VENUES) {
                boolean exists;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COUNT(*) FROM venues WHERE name = ?")) {
                    ps.setString(1, v[0]);
                    try (ResultSet rs = ps.executeQuery()) {
                        exists = rs.next() && rs.getInt(1) > 0;
                    }
                }
                if (exists) continue;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO venues (name, capacity_limit, location) VALUES (?,?,?)")) {
                    ps.setString(1, v[0]);
                    ps.setInt(2, Integer.parseInt(v[1]));
                    ps.setString(3, v[2]);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}