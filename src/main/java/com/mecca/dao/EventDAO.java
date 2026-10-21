// src/main/java/com/mecca/dao/EventDAO.java
package com.mecca.dao;

import com.mecca.db.DatabaseConnection;
import com.mecca.model.Event;
import com.mecca.model.Venue;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Database access for events (and the venue list used by the event form). */
public class EventDAO {

    private static final String SELECT_BASE =
        "SELECT e.event_id, e.club_id, e.title, e.description, e.date_time, e.capacity, " +
        "e.venue_id, e.ktu_code, e.level, e.status, c.name AS club_name, v.name AS venue_name " +
        "FROM events e " +
        "LEFT JOIN clubs c ON e.club_id = c.club_id " +
        "LEFT JOIN venues v ON e.venue_id = v.venue_id ";

    /** Inserts an event and returns the new event_id (or -1). */
    public int insert(Event e) throws SQLException {
        String sql = "INSERT INTO events (club_id, title, description, date_time, capacity, " +
                     "venue_id, ktu_code, level, status) VALUES (?,?,?,?,?,?,?,?,?)";
        Connection con = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getClubId());
            ps.setString(2, e.getTitle());
            ps.setString(3, e.getDescription());
            ps.setString(4, e.getDateTime());
            ps.setInt(5, e.getCapacity());
            ps.setInt(6, e.getVenueId());
            if (e.getKtuCode() == null || e.getKtuCode().isEmpty()) ps.setNull(7, Types.VARCHAR);
            else ps.setString(7, e.getKtuCode());
            ps.setInt(8, e.getLevel());
            ps.setString(9, e.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean update(Event e) throws SQLException {
        String sql = "UPDATE events SET title=?, description=?, date_time=?, capacity=?, " +
                     "venue_id=?, ktu_code=?, level=? WHERE event_id=?";
        Connection con = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getTitle());
            ps.setString(2, e.getDescription());
            ps.setString(3, e.getDateTime());
            ps.setInt(4, e.getCapacity());
            ps.setInt(5, e.getVenueId());
            if (e.getKtuCode() == null || e.getKtuCode().isEmpty()) ps.setNull(6, Types.VARCHAR);
            else ps.setString(6, e.getKtuCode());
            ps.setInt(7, e.getLevel());
            ps.setInt(8, e.getEventId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int eventId, String status) throws SQLException {
        String sql = "UPDATE events SET status=? WHERE event_id=?";
        Connection con = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, eventId);
            return ps.executeUpdate() > 0;
        }
    }

    public Event findById(int eventId) throws SQLException {
        String sql = SELECT_BASE + "WHERE e.event_id = ?";
        Connection con = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Event> findActive() throws SQLException {
        return runList(SELECT_BASE + "WHERE e.status = 'ACTIVE' ORDER BY e.date_time", -1);
    }

    public List<Event> findByClub(int clubId) throws SQLException {
        return runList(SELECT_BASE + "WHERE e.club_id = ? ORDER BY e.date_time", clubId);
    }

    /** All venues (for the venue dropdown). */
    public List<Venue> findAllVenues() throws SQLException {
        List<Venue> list = new ArrayList<>();
        String sql = "SELECT venue_id, name, capacity_limit, location FROM venues ORDER BY name";
        Connection con = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Venue(rs.getInt("venue_id"), rs.getString("name"),
                        rs.getInt("capacity_limit"), rs.getString("location")));
            }
        }
        return list;
    }

    private List<Event> runList(String sql, int param) throws SQLException {
        List<Event> list = new ArrayList<>();
        Connection con = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (param >= 0) ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private Event map(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setEventId(rs.getInt("event_id"));
        e.setClubId(rs.getInt("club_id"));
        e.setTitle(rs.getString("title"));
        e.setDescription(rs.getString("description"));
        e.setDateTime(rs.getString("date_time"));
        e.setCapacity(rs.getInt("capacity"));
        e.setVenueId(rs.getInt("venue_id"));
        e.setKtuCode(rs.getString("ktu_code"));
        e.setLevel(rs.getInt("level"));
        e.setStatus(rs.getString("status"));
        e.setClubName(rs.getString("club_name"));
        e.setVenueName(rs.getString("venue_name"));
        return e;
    }
}