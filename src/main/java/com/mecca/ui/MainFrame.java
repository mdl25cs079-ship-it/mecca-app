package com.mecca.ui;

import com.mecca.ui.admin.AnalyticsPanel;
import com.mecca.ui.admin.ClaimsPanel;
import com.mecca.ui.admin.ClubsPanel;
import com.mecca.ui.admin.UsersPanel;
import com.mecca.ui.organizer.AttendancePanel;
import com.mecca.ui.organizer.CreateEventPanel;
import com.mecca.ui.organizer.MyEventsPanel;
import com.mecca.ui.student.BrowseEventsPanel;
import com.mecca.ui.student.ClaimFormPanel;
import com.mecca.ui.student.FeedbackPanel;
import com.mecca.ui.student.HistoryPanel;
import com.mecca.ui.student.LeaderboardPanel;
import com.mecca.ui.student.PointsPanel;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/** STUB main window. Owner: M2. Shows every panel in a tab so the whole team can see their panel run. M2 replaces with role-based tabs. */
public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("MECCA - MainFrame (STUB, M2 will replace)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Browse Events", new BrowseEventsPanel());
        tabs.addTab("History", new HistoryPanel());
        tabs.addTab("My Points", new PointsPanel());
        tabs.addTab("Claim Form", new ClaimFormPanel());
        tabs.addTab("Leaderboard", new LeaderboardPanel());
        tabs.addTab("Feedback", new FeedbackPanel());
        tabs.addTab("Create Event", new CreateEventPanel());
        tabs.addTab("My Events", new MyEventsPanel());
        tabs.addTab("Attendance", new AttendancePanel());
        tabs.addTab("Clubs", new ClubsPanel());
        tabs.addTab("Users", new UsersPanel());
        tabs.addTab("Analytics", new AnalyticsPanel());
        tabs.addTab("Claims", new ClaimsPanel());
        add(tabs);
    }
}
