// src/main/java/com/mecca/ui/MainFrame.java
package com.mecca.ui;

import com.mecca.model.User;
import com.mecca.service.AuthService;
import com.mecca.ui.auth.LoginFrame;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.lang.reflect.Method;

/** Main window: header (name, role, Logout) + tabs that depend on the role. Owner: M2 (updated by M1). */
public class MainFrame extends JFrame {

    // {tab title, panel class}. Panels belong to other members, so they are loaded by name.
    private static final String[][] STUDENT_TABS = {
        {"Browse Events", "com.mecca.ui.student.BrowseEventsPanel"},
        {"History",       "com.mecca.ui.student.HistoryPanel"},
        {"My Points",     "com.mecca.ui.student.PointsPanel"},
        {"Claim Points",  "com.mecca.ui.student.ClaimFormPanel"},
        {"Leaderboard",   "com.mecca.ui.student.LeaderboardPanel"},
        {"Feedback",      "com.mecca.ui.student.FeedbackPanel"}
    };
    private static final String[][] ORGANIZER_TABS = {
        {"Create Event",   "com.mecca.ui.organizer.CreateEventPanel"},
        {"My Events",      "com.mecca.ui.organizer.MyEventsPanel"},
        {"Attendance",     "com.mecca.ui.organizer.AttendancePanel"},
        {"Club Analytics", "com.mecca.ui.organizer.ClubAnalyticsPanel"}
    };
    private static final String[][] ADMIN_TABS = {
        {"Clubs",         "com.mecca.ui.admin.ClubsPanel"},
        {"Users",         "com.mecca.ui.admin.UsersPanel"},
        {"Analytics",     "com.mecca.ui.admin.AnalyticsPanel"},
        {"Claims",        "com.mecca.ui.admin.ClaimsPanel"}
    };

    /** When a tab is opened, the first of these no-argument methods found on the panel is called. */
    private static final String[] REFRESH_METHODS =
            {"refresh", "loadEvents", "loadHistory", "loadClaims", "load"};

    public MainFrame() {
        User user = AuthService.getCurrentUser();
        String who = (user == null) ? "Guest" : user.getName() + " (" + user.getRole() + ")";
        setTitle("MECCA - " + who);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(buildHeader(who), BorderLayout.NORTH);
        add(buildTabs(user), BorderLayout.CENTER);
    }

    /** Blue header with the user's name, role and a Logout button. */
    private JPanel buildHeader(String who) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x1E3A8A));
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel title = new JLabel("MECCA   |   " + who);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> logout());

        header.add(title, BorderLayout.WEST);
        header.add(logout, BorderLayout.EAST);
        return header;
    }

    private void logout() {
        AuthService.logout();
        dispose();
        new LoginFrame().setVisible(true);
    }

    /** Picks the tab list for the user's role and refreshes a tab whenever it is opened. */
    private JTabbedPane buildTabs(User user) {
        JTabbedPane tabs = new JTabbedPane();
        if (user == null || user.getRole() == null) {
            return tabs;
        }
        String[][] list;
        switch (user.getRole()) {
            case "STUDENT":   list = STUDENT_TABS;   break;
            case "ORGANIZER": list = ORGANIZER_TABS; break;
            case "ADMIN":     list = ADMIN_TABS;     break;
            default:          list = new String[0][];
        }
        for (String[] t : list) {
            tabs.addTab(t[0], loadPanel(t[1]));
        }
        // Every time the user opens a tab, reload its data so nothing is ever stale.
        tabs.addChangeListener(e -> refreshSelected(tabs));
        return tabs;
    }

    private void refreshSelected(JTabbedPane tabs) {
        Component selected = tabs.getSelectedComponent();
        if (selected == null) {
            return;
        }
        for (String name : REFRESH_METHODS) {
            try {
                Method m = selected.getClass().getDeclaredMethod(name);
                m.setAccessible(true);
                m.invoke(selected);
                return;
            } catch (NoSuchMethodException ignored) {
                // this panel has no method with that name, try the next one
            } catch (Exception ex) {
                System.err.println("Could not refresh " + selected.getClass().getSimpleName() + ": " + ex);
                return;
            }
        }
    }

    /** Creates the panel by class name; shows a placeholder if it is missing or fails. */
    private JComponent loadPanel(String className) {
        try {
            Object panel = Class.forName(className).getDeclaredConstructor().newInstance();
            if (panel instanceof JComponent) {
                return (JComponent) panel;
            }
        } catch (Exception | LinkageError e) {
            System.err.println("Could not load " + className + ": " + e);
        }
        String simple = className.substring(className.lastIndexOf('.') + 1);
        return new JLabel(simple + " is not available yet", SwingConstants.CENTER);
    }
}