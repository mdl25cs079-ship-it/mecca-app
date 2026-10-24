// src/main/java/com/mecca/ui/organizer/AttendancePanel.java
package com.mecca.ui.organizer;

import com.mecca.model.Event;
import com.mecca.model.Organizer;
import com.mecca.model.RegistrantRow;
import com.mecca.model.User;
import com.mecca.service.AttendanceService;
import com.mecca.service.AuthService;
import com.mecca.service.EventService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Organizer screen: pick an event and mark registered students present. Owner: M6 (updated by M1). */
public class AttendancePanel extends JPanel {

    private static final int COL_REG_ID = 0;
    private static final int COL_PRESENT = 3;

    private final JComboBox<EventItem> eventCombo = new JComboBox<>();
    private final Set<Integer> lockedRegIds = new HashSet<>(); // already marked rows
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel messageLabel = new JLabel(" ");
    private final JButton markButton = new JButton("Mark Selected Present");
    private final JButton selectAllButton = new JButton("Select All");
    private boolean loadingEvents = false;

    public AttendancePanel() {
        setLayout(new BorderLayout(8, 8));

        // Table: checkbox column, locked for students already marked present
        tableModel = new DefaultTableModel(new Object[] { "Reg ID", "Student", "Roll No", "Present" }, 0) {
            @Override
            public Class<?> getColumnClass(int col) {
                return col == COL_PRESENT ? Boolean.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return col == COL_PRESENT && !lockedRegIds.contains(getValueAt(row, COL_REG_ID));
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Event:"));
        top.add(eventCombo);
        top.add(messageLabel);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(selectAllButton);
        bottom.add(markButton);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        eventCombo.addActionListener(e -> {
            if (!loadingEvents) {
                loadRegistrants();
            }
        });
        markButton.addActionListener(e -> markSelected());
        selectAllButton.addActionListener(e -> selectAll());

        refresh();
    }

    /** Reloads the organizer's events (called every time the tab is opened). Keeps the chosen event. */
    public void refresh() {
        int previousId = -1;
        EventItem previous = (EventItem) eventCombo.getSelectedItem();
        if (previous != null) {
            previousId = previous.event.getEventId();
        }
        loadingEvents = true;
        try {
            eventCombo.removeAllItems();
            EventItem toSelect = null;
            for (EventItem item : findEvents()) {
                eventCombo.addItem(item);
                if (item.event.getEventId() == previousId) {
                    toSelect = item;
                }
            }
            if (toSelect != null) {
                eventCombo.setSelectedItem(toSelect);
            }
        } finally {
            loadingEvents = false;
        }
        loadRegistrants();
    }

    /** Events of the organizer's club that are not cancelled. Sets the message and button state. */
    private List<EventItem> findEvents() {
        List<EventItem> items = new ArrayList<>();
        markButton.setEnabled(false);
        selectAllButton.setEnabled(false);
        try {
            User user = AuthService.getCurrentUser();
            if (!(user instanceof Organizer)) {
                messageLabel.setText("Please log in as an organizer.");
                return items;
            }
            int clubId = ((Organizer) user).getManagedClubId();
            if (clubId <= 0) {
                messageLabel.setText("Your account is not linked to a club yet. Ask the admin to assign one.");
                return items;
            }
            List<Event> events = EventService.getEventsByClub(clubId);
            if (events != null) {
                for (Event ev : events) {
                    if (!"CANCELLED".equals(ev.getStatus())) {
                        items.add(new EventItem(ev));
                    }
                }
            }
            if (items.isEmpty()) {
                messageLabel.setText("Your club has no events yet. Create one in the Create Event tab.");
            } else {
                messageLabel.setText(" ");
                markButton.setEnabled(true);
                selectAllButton.setEnabled(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load your events. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        return items;
    }

    /** Fills the table with the registrants of the selected event. */
    private void loadRegistrants() {
        tableModel.setRowCount(0);
        lockedRegIds.clear();
        EventItem item = (EventItem) eventCombo.getSelectedItem();
        if (item == null) {
            return;
        }
        try {
            List<RegistrantRow> rows = AttendanceService.getRegistrantsForEvent(item.event.getEventId());
            for (RegistrantRow r : rows) {
                if (r.isAttended()) {
                    lockedRegIds.add(r.getRegId());
                }
                tableModel.addRow(new Object[] {
                        r.getRegId(), r.getStudentName(), r.getRollNumber(), r.isAttended() });
            }
            messageLabel.setText(rows.isEmpty() ? "No registrations for this event yet." : " ");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load registrations. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectAll() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int regId = (Integer) tableModel.getValueAt(i, COL_REG_ID);
            if (!lockedRegIds.contains(regId)) {
                tableModel.setValueAt(Boolean.TRUE, i, COL_PRESENT);
            }
        }
    }

    /** Marks every ticked (and not yet locked) row as present. */
    private void markSelected() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing(); // commit the last checkbox click
        }
        int marked = 0;
        int failed = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int regId = (Integer) tableModel.getValueAt(i, COL_REG_ID);
            boolean ticked = Boolean.TRUE.equals(tableModel.getValueAt(i, COL_PRESENT));
            if (!ticked || lockedRegIds.contains(regId)) {
                continue;
            }
            try {
                AttendanceService.markAttendance(regId);
                marked++;
            } catch (Exception ex) {
                failed++;
            }
        }

        if (marked == 0 && failed == 0) {
            JOptionPane.showMessageDialog(this, "Tick at least one student who is not yet marked.",
                    "Nothing to mark", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (failed > 0) {
            JOptionPane.showMessageDialog(this,
                    marked + " marked present, " + failed + " could not be saved. Please try again.",
                    "Partly done", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    marked + " student(s) marked present. KTU points were awarded automatically.",
                    "Done", JOptionPane.INFORMATION_MESSAGE);
        }
        loadRegistrants(); // refresh so marked rows become locked
    }

    /** Wrapper so the combo shows the event title. */
    private static class EventItem {
        private final Event event;

        EventItem(Event event) {
            this.event = event;
        }

        @Override
        public String toString() {
            return event.getTitle() + " (" + event.getDateTime() + ")";
        }
    }
}