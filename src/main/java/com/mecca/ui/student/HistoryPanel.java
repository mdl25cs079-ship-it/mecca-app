package com.mecca.ui.student;

import com.mecca.model.Event;
import com.mecca.model.Registration;
import com.mecca.model.User;
import com.mecca.service.AttendanceService;
import com.mecca.service.AuthService;
import com.mecca.service.EventService;
import com.mecca.service.RegistrationService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

/**
 * Student view: the logged-in student's registrations.
 *
 * Contracts assumed:
 *   User        AuthService.getCurrentUser()            (null if nobody is logged in)
 *   Event       EventService.getEventById(int)          (null if not found)
 *   String      AttendanceService.getStatus(int userId, int eventId)
 *               (null or empty if attendance has not been marked)
 */
public class HistoryPanel extends JPanel {

    private static final String[] COLUMNS =
            {"Event Title", "Event Date", "Registration Status", "Attendance Status"};

    private static final String NOT_MARKED = "Not marked";

    private final RegistrationService registrationService = new RegistrationService();

    private final JLabel messageLabel = new JLabel(" ");
    private final JButton refreshButton = new JButton("Refresh");

    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(tableModel);

    public HistoryPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.add(new JLabel("My Registrations"));
        add(top, BorderLayout.NORTH);

        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(messageLabel, BorderLayout.WEST);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(refreshButton);
        bottom.add(buttons, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> loadHistory());

        loadHistory();
    }

    /** Reloads the logged-in student's registrations. */
    public void loadHistory() {
        tableModel.setRowCount(0);

        User user = AuthService.getCurrentUser();
        if (user == null) {
            messageLabel.setText("Please log in to see your registrations.");
            return;
        }

        List<Registration> history = registrationService.getHistory(user.getUserId());
        if (history == null || history.isEmpty()) {
            messageLabel.setText("You have not registered for any events yet.");
            return;
        }
        messageLabel.setText(" ");

        for (Registration reg : history) {
            Event event = EventService.getEventById(reg.getEventId());

            String title = event != null ? event.getTitle() : "(Event removed)";
            String date = event != null ? String.valueOf(event.getDateTime()) : "-";

            String attendance = null;
            try {
                attendance = AttendanceService.getStatus(user.getUserId(), reg.getEventId());
            } catch (Exception e) {
                System.err.println("Could not load attendance for event "
                        + reg.getEventId() + ": " + e.getMessage());
            }
            if (attendance == null || attendance.trim().isEmpty()) {
                attendance = NOT_MARKED;
            }

            tableModel.addRow(new Object[]{title, date, reg.getStatus(), attendance});
        }
    }
}
