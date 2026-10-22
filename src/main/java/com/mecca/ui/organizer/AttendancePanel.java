package com.mecca.ui.organizer;

import com.mecca.model.Event;
import com.mecca.model.Organizer;
import com.mecca.model.RegistrantRow;
import com.mecca.model.User;
import com.mecca.service.AttendanceService;
import com.mecca.service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.List;

/** Organizer screen to log attendance for an event. Owner: M6. */
public class AttendancePanel extends JPanel {

    private final JComboBox<Event> eventCombo = new JComboBox<>();
    private final JLabel summary = new JLabel(" ");
    private final List<RegistrantRow> rows = new ArrayList<>();
    private final DefaultTableModel model;
    private boolean loadingEvents = false;

    public AttendancePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        model = new DefaultTableModel(new Object[]{"Present", "Student", "Roll No", "Status"}, 0) {
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Boolean.class : String.class;
            }
            @Override public boolean isCellEditable(int r, int c) {
                return c == 0 && r < rows.size() && !rows.get(r).isAttended();
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.getColumnModel().getColumn(0).setMaxWidth(80);

        eventCombo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                    boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof Event) {
                    Event e = (Event) value;
                    setText(e.getTitle() + "   (" + e.getDateTime() + ")");
                }
                return this;
            }
        });
        eventCombo.addActionListener(e -> { if (!loadingEvents) loadRegistrants(); });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.add(new JLabel("Event:"));
        top.add(eventCombo);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> reloadEvents());
        top.add(refresh);

        JButton selectAll = new JButton("Select All");
        selectAll.addActionListener(e -> {
            for (int i = 0; i < rows.size(); i++) {
                if (!rows.get(i).isAttended()) model.setValueAt(Boolean.TRUE, i, 0);
            }
        });
        JButton mark = new JButton("Mark Selected Present");
        mark.addActionListener(e -> markSelected());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(summary, BorderLayout.WEST);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(selectAll);
        buttons.add(mark);
        bottom.add(buttons, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        // Reload every time this tab becomes visible so new events and registrations appear.
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                reloadEvents();
            }
        });
    }

    /** Organizer sees only their own club; anyone else (admin or dev mode) sees all events. */
    private int currentClubId() {
        User u = AuthService.getCurrentUser();
        if (u instanceof Organizer) {
            return ((Organizer) u).getManagedClubId();
        }
        return 0;
    }

    private void reloadEvents() {
        try {
            Event previous = (Event) eventCombo.getSelectedItem();
            loadingEvents = true;
            eventCombo.removeAllItems();
            List<Event> events = AttendanceService.getEventsForAttendance(currentClubId());
            Event toSelect = null;
            for (Event ev : events) {
                eventCombo.addItem(ev);
                if (previous != null && previous.getEventId() == ev.getEventId()) toSelect = ev;
            }
            if (toSelect != null) eventCombo.setSelectedItem(toSelect);
            loadingEvents = false;
            loadRegistrants();
        } catch (RuntimeException ex) {
            loadingEvents = false;
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Attendance", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadRegistrants() {
        rows.clear();
        model.setRowCount(0);
        Event ev = (Event) eventCombo.getSelectedItem();
        if (ev == null) {
            summary.setText("No events found for your club.");
            return;
        }
        try {
            rows.addAll(AttendanceService.getRegistrantsForEvent(ev.getEventId()));
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Attendance", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int present = 0;
        for (RegistrantRow r : rows) {
            if (r.isAttended()) present++;
            model.addRow(new Object[]{r.isAttended(), r.getStudentName(), r.getRollNumber(),
                    r.isAttended() ? "Present" : "Not marked"});
        }
        summary.setText("Present: " + present + " / " + rows.size() + " registered");
    }

    private void markSelected() {
        int marked = 0;
        int failed = 0;
        for (int i = 0; i < rows.size(); i++) {
            Object value = model.getValueAt(i, 0);
            boolean ticked = value instanceof Boolean && (Boolean) value;
            if (ticked && !rows.get(i).isAttended()) {
                try {
                    AttendanceService.markAttendance(rows.get(i).getRegId());
                    marked++;
                } catch (RuntimeException ex) {
                    failed++;
                }
            }
        }
        if (marked == 0 && failed == 0) {
            JOptionPane.showMessageDialog(this, "Tick at least one student who is not already marked.",
                    "Attendance", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        loadRegistrants();
        String msg = marked + " student(s) marked present. KTU points are awarded automatically.";
        if (failed > 0) msg += "\n" + failed + " could not be saved.";
        JOptionPane.showMessageDialog(this, msg, "Attendance", JOptionPane.INFORMATION_MESSAGE);
    }
}
