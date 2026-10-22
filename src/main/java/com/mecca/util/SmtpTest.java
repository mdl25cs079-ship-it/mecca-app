package com.mecca.util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.FileInputStream;
import java.util.Properties;

public class SmtpTest {
    public static void main(String[] args) {
        try {
            Properties cfg = new Properties();
            try (FileInputStream in = new FileInputStream("config.properties")) {
                cfg.load(in);
            }
            String user = cfg.getProperty("MAIL_USER", "").trim();
            String pass = cfg.getProperty("MAIL_PASS", "").trim();
            Properties p = new Properties();
            p.put("mail.smtp.auth", "true");
            p.put("mail.smtp.starttls.enable", "true");
            p.put("mail.smtp.host", cfg.getProperty("SMTP_HOST", "smtp.gmail.com").trim());
            p.put("mail.smtp.port", cfg.getProperty("SMTP_PORT", "587").trim());
            p.put("mail.smtp.connectiontimeout", "8000");
            p.put("mail.smtp.timeout", "8000");
            Session s = Session.getInstance(p, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass);
                }
            });
            MimeMessage m = new MimeMessage(s);
            m.setFrom(new InternetAddress(user));
            m.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user));
            m.setSubject("MECCA SMTP test");
            m.setText("If you can read this, live email works.");
            Transport.send(m);
            System.out.println("SUCCESS: test email sent to " + user);
        } catch (Throwable x) {
            System.out.println("FAILED: " + x);
        }
        System.exit(0);
    }
}
