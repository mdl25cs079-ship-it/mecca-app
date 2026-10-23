// src/main/java/com/mecca/dao/CategoryDAO.java
package com.mecca.dao;

import com.mecca.db.DatabaseConnection;
import com.mecca.model.KTUCategory;
import com.mecca.util.KtuSeeder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Database access for KTU activity point categories.
 * Owner: M7.
 */
public class CategoryDAO {

    private static Connection con() {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Retrieves all KTU categories ordered by group_no and code.
     * Ensures categories are seeded if the table is empty.
     *
     * @return List of all KTUCategory objects.
     */
    public static List<KTUCategory> findAll() {
        KtuSeeder.seedIfEmpty();
        String sql = "SELECT code, group_no, name, max_points, "
                   + "points_l1, points_l2, points_l3, points_l4, points_l5 "
                   + "FROM ktu_categories ORDER BY group_no ASC, code ASC";
        List<KTUCategory> list = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load KTU categories: " + ex.getMessage(), ex);
        }
        return list;
    }

    /**
     * Finds a KTU category by its unique code.
     * Ensures categories are seeded if the table is empty.
     *
     * @param code The KTU category code (e.g. "1.1", "2.1").
     * @return The KTUCategory if found, or null if code is null/blank or not found.
     */
    public static KTUCategory findByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        KtuSeeder.seedIfEmpty();
        String sql = "SELECT code, group_no, name, max_points, "
                   + "points_l1, points_l2, points_l3, points_l4, points_l5 "
                   + "FROM ktu_categories WHERE code = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not find KTU category with code " + code + ": " + ex.getMessage(), ex);
        }
        return null;
    }

    private static KTUCategory map(ResultSet rs) throws SQLException {
        int[] pointsByLevel = new int[]{
                rs.getInt("points_l1"),
                rs.getInt("points_l2"),
                rs.getInt("points_l3"),
                rs.getInt("points_l4"),
                rs.getInt("points_l5")
        };
        return new KTUCategory(
                rs.getString("code"),
                rs.getInt("group_no"),
                rs.getString("name"),
                rs.getInt("max_points"),
                pointsByLevel
        );
    }
}
