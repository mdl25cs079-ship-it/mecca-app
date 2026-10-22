package com.mecca.util;

import com.mecca.db.DatabaseConnection;
import com.mecca.model.Event;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class ICSGenerator {
    private static final DateTimeFormatter IN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter OUT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    public static File generate(Event e) {
        try {
            LocalDateTime s = LocalDateTime.parse(e.getDateTime().trim(), IN);
            LocalDateTime en = s.plusHours(2);
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            File dir = new File("outbox");
            dir.mkdirs();
            File f = new File(dir, "event_" + e.getEventId() + ".ics");
            String nl = "\r\n";
            StringBuilder b = new StringBuilder();
            b.append("BEGIN:VCALENDAR").append(nl);
            b.append("VERSION:2.0").append(nl);
            b.append("PRODID:-//MECCA//MEC Campus Community App//EN").append(nl);
            b.append("CALSCALE:GREGORIAN").append(nl);
            b.append("METHOD:PUBLISH").append(nl);
            b.append("BEGIN:VEVENT").append(nl);
            b.append("UID:event-").append(e.getEventId()).append("@mecca.mec").append(nl);
            b.append("DTSTAMP:").append(now.format(OUT)).append("Z").append(nl);
            b.append("DTSTART:").append(s.format(OUT)).append(nl);
            b.append("DTEND:").append(en.format(OUT)).append(nl);
            b.append("SUMMARY:").append(esc(e.getTitle())).append(nl);
            b.append("LOCATION:").append(esc(venueName(e.getVenueId()))).append(nl);
            b.append("DESCRIPTION:").append(esc(e.getDescription())).append(nl);
            b.append("END:VEVENT").append(nl);
            b.append("END:VCALENDAR").append(nl);
            Files.write(f.toPath(), b.toString().getBytes(StandardCharsets.UTF_8));
            return f;
        } catch (Exception x) {
            System.err.println("ICS failed: " + x.getMessage());
            return null;
        }
    }

    public static String venueName(int venueId) {
        try {
            Connection c = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement p = c.prepareStatement("SELECT name, location FROM venues WHERE venue_id = ?")) {
                p.setInt(1, venueId);
                try (ResultSet r = p.executeQuery()) {
                    if (r.next()) {
                        String loc = r.getString("location");
                        String n = r.getString("name");
                        return loc == null || loc.isEmpty() ? n : n + ", " + loc;
                    }
                }
            }
        } catch (Exception x) {
            System.err.println("Venue lookup failed: " + x.getMessage());
        }
        return "MEC Campus";
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,")
                .replace("\r\n", "\\n").replace("\n", "\\n");
    }
}
