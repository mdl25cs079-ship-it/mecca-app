package com.mecca.service;

import com.mecca.db.DatabaseConnection;
import com.mecca.model.Event;
import com.mecca.util.Constants;
import com.mecca.util.ICSGenerator;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class NotificationService {
    public static void sendConfirmation(int userId, int eventId) {
        try {
            Thread t = new Thread(() -> work(userId, eventId), "mecca-mail");
            t.start();
        } catch (Throwable x) {
            System.err.println("Notify start failed: " + x.getMessage());
        }
    }

    private static void work(int uid, int eid) {
        try {
            Event ev = loadEvent(eid);
            String[] u = loadUser(uid);
            if (ev == null || u == null) {
                System.err.println("Notify skipped: user or event not found");
                return;
            }
            File ics = ICSGenerator.generate(ev);
            String body = body(u[0], ev);
            Properties cfg = config();
            String mode = cfg.getProperty("MAIL_MODE", Constants.MAIL_MOCK).trim().toUpperCase();
            boolean sent = false;
            if (Constants.MAIL_LIVE.equals(mode)) {
                sent = sendLive(cfg, u[1], ev, body, ics);
            }
            if (sent) {
                popup("Confirmation email sent to " + u[1]);
            } else {
                saveMock(ev, body);
                popup("Confirmation sent (saved to outbox)");
            }
        } catch (Throwable x) {
            System.err.println("Notify failed: " + x.getMessage());
        }
    }

    private static boolean sendLive(Properties cfg, String to, Event ev, String body, File ics) {
        try {
            String host = cfg.getProperty("SMTP_HOST", "smtp.gmail.com").trim();
            String port = cfg.getProperty("SMTP_PORT", "587").trim();
            String user = cfg.getProperty("MAIL_USER", "").trim();
            String pass = cfg.getProperty("MAIL_PASS", "").trim();
            Properties p = new Properties();
            p.put("mail.smtp.auth", "true");
            p.put("mail.smtp.starttls.enable", "true");
            p.put("mail.smtp.host", host);
            p.put("mail.smtp.port", port);
            p.put("mail.smtp.connectiontimeout", "8000");
            p.put("mail.smtp.timeout", "8000");
            p.put("mail.smtp.writetimeout", "8000");
            Session s = Session.getInstance(p, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass);
                }
            });
            MimeMessage m = new MimeMessage(s);
            m.setFrom(new InternetAddress(user));
            m.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            m.setSubject("MECCA: Registration confirmed - " + ev.getTitle(), "UTF-8");
            MimeMultipart mp = new MimeMultipart();
            MimeBodyPart tp = new MimeBodyPart();
            tp.setText(body, "UTF-8");
            mp.addBodyPart(tp);
            if (ics != null) {
                MimeBodyPart ap = new MimeBodyPart();
                ap.attachFile(ics);
                mp.addBodyPart(ap);
            }
            m.setContent(mp);
            Transport.send(m);
            return true;
        } catch (Throwable x) {
            System.err.println("Live mail failed, using MOCK: " + x.getMessage());
            return false;
        }
    }

    private static void saveMock(Event ev, String body) throws Exception {
        File dir = new File("outbox");
        dir.mkdirs();
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File f = new File(dir, ts + "_confirmation.txt");
        Files.write(f.toPath(), body.getBytes(StandardCharsets.UTF_8));
    }

    private static String body(String name, Event ev) {
        String nl = "\n";
        return "Hello " + name + "," + nl + nl
                + "Your registration is confirmed." + nl + nl
                + "Event: " + ev.getTitle() + nl
                + "When: " + ev.getDateTime() + nl
                + "Where: " + ICSGenerator.venueName(ev.getVenueId()) + nl + nl
                + "A calendar file (.ics) is attached. Open it to add the event to your calendar." + nl + nl
                + "MECCA - MEC Campus Community App" + nl;
    }

    private static Properties config() {
        Properties p = new Properties();
        File f = new File("config.properties");
        if (f.exists()) {
            try (FileInputStream in = new FileInputStream(f)) {
                p.load(in);
            } catch (Exception x) {
                System.err.println("Config read failed: " + x.getMessage());
            }
        }
        return p;
    }

    private static Event loadEvent(int eid) throws Exception {
        Connection c = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement p = c.prepareStatement("SELECT * FROM events WHERE event_id = ?")) {
            p.setInt(1, eid);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) return null;
                Event e = new Event();
                e.setEventId(r.getInt("event_id"));
                e.setClubId(r.getInt("club_id"));
                e.setTitle(r.getString("title"));
                e.setDescription(r.getString("description"));
                e.setDateTime(r.getString("date_time"));
                e.setCapacity(r.getInt("capacity"));
                e.setVenueId(r.getInt("venue_id"));
                e.setKtuCode(r.getString("ktu_code"));
                e.setLevel(r.getInt("level"));
                e.setStatus(r.getString("status"));
                return e;
            }
        }
    }

    private static String[] loadUser(int uid) throws Exception {
        Connection c = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement p = c.prepareStatement("SELECT name, email FROM users WHERE user_id = ?")) {
            p.setInt(1, uid);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) return null;
                return new String[]{r.getString("name"), r.getString("email")};
            }
        }
    }

    private static void popup(String msg) {
        if (GraphicsEnvironment.isHeadless()) return;
        SwingUtilities.invokeLater(() -> {
            JDialog d = new JDialog((Frame) null, "MECCA", false);
            JLabel l = new JLabel(msg);
            l.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
            d.add(l);
            d.pack();
            d.setLocationRelativeTo(null);
            d.setAlwaysOnTop(true);
            d.setVisible(true);
            Timer t = new Timer(3000, a -> d.dispose());
            t.setRepeats(false);
            t.start();
        });
    }
}