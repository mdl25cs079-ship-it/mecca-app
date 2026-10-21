// src/main/java/com/mecca/util/SeedVenues.java
package com.mecca.util;

import com.mecca.db.DatabaseConnection;
import java.sql.*;

/** Adds a few venues if the venues table is empty. */
public class SeedVenues {

    public static void seedIfEmpty() {
        Connection con = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement count = con.prepareStatement("SELECT COUNT(*) FROM venues");
             ResultSet rs = count.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) return;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return;
        }

        String[] names = {"Main Auditorium", "Seminar Hall", "Computer Lab 1", "Open Air Stage", "Sports Ground"};
        int[] caps = {500, 150, 60, 300, 1000};
        String[] locs = {"Main Block", "Admin Block", "CS Department", "Central Lawn", "Behind Library"};

        String sql = "INSERT INTO venues (name, capacity_limit, location) VALUES (?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < names.length; i++) {
                ps.setString(1, names[i]);
                ps.setInt(2, caps[i]);
                ps.setString(3, locs[i]);
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}