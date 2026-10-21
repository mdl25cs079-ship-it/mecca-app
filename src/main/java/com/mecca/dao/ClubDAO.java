// src/main/java/com/mecca/dao/ClubDAO.java
package com.mecca.dao;

import com.mecca.db.DatabaseConnection;
import com.mecca.model.Club;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Database access for the clubs table. */
public class ClubDAO {

    public List<Club> findAll() throws SQLException {
        List<Club> list = new ArrayList<>();
        String sql = "SELECT club_id, name, description, status, engagement_score FROM clubs";
        Connection con = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Club findById(int clubId) throws SQLException {
        String sql = "SELECT club_id, name, description, status, engagement_score FROM clubs WHERE club_id = ?";
        Connection con = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    private Club map(ResultSet rs) throws SQLException {
        return new Club(rs.getInt("club_id"), rs.getString("name"),
                rs.getString("description"), rs.getString("status"),
                rs.getDouble("engagement_score"));
    }
}