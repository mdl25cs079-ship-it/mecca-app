// src/main/java/com/mecca/ui/organizer/MyEventsPanel.java
package com.mecca.ui.organizer;

import com.mecca.util.Constants;
import com.mecca.model.Event;
import com.mecca.model.Organizer;
import com.mecca.model.User;
import com.mecca.service.AuthService;
import com.mecca.service.EventService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Table of the organizer's events with Edit and Cancel buttons. */
public class MyEventsPanel extends JPanel {

    private final EventService eventService = new EventService();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Title", "Date", "Capacity", "KTU", "Level", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private List<Event> events = new ArrayList<>();

    public MyEventsPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        JButton edit = new JButton("Edit");
        JButton cancel = new JButton("Cancel Event");
        refresh.addActionListener(e -> loadEvents());
        edit.addActionListener(e -> editSelected());
        cancel.addActionListener(e -> cancelSelected());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(refresh);
        buttons.add(edit);
        buttons.add(cancel);
        add(buttons, BorderLayout.SOUTH);

        loadEvents();
    }

    private int currentClubId() {
        User u = AuthService.getCurrentUser();
        if (u instanceof Organizer) return ((Organizer) u).getManagedClubId();
        return -1;
    }

    private void loadEvents() {
        tableModel.setRowCount(0);
        try {
            events = eventService.getEventsByClub(currentClubId());
            for (Event e : events) {
                tableModel.addRow(new Object[]{e.getEventId(), e.getTitle(), e.getDateTime(),
                        e.getCapacity(), e.getKtuCode() == null ? "-" : e.getKtuCode(),
                        levelName(e.getLevel()), e.getStatus()});
            }
            if (events.isEmpty()) {
                // Friendly empty state: table is simply empty
                setToolTipText("No events yet. Create one in the Create Event tab.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not load events.");
        }
    }

    private Event selectedEvent() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= events.size()) {
            JOptionPane.showMessageDialog(this, "Please select an event first.");
            return null;
        }
        return events.get(row);
    }
    
        private String levelName(int level) {
        return level >= 1 && level <= 5 ? Constants.LEVEL_NAMES[level] : "-";
    }

    private void editSelected() {
        Event ev = selectedEvent();
        if (ev == null) return;
        if (!"ACTIVE".equals(ev.getStatus())) {
            JOptionPane.showMessageDialog(this, "Only ACTIVE events can be edited.");
            return;
        }
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Event", true);
        dialog.setContentPane(new CreateEventPanel(ev, () -> {
            dialog.dispose();
            loadEvents();
        }));
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void cancelSelected() {
        Event ev = selectedEvent();
        if (ev == null) return;
        if (!"ACTIVE".equals(ev.getStatus())) {
            JOptionPane.showMessageDialog(this, "This event is already " + ev.getStatus() + ".");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Cancel \"" + ev.getTitle() + "\"? Students will no longer see it.",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;
        try {
            if (eventService.cancelEvent(ev.getEventId())) {
                JOptionPane.showMessageDialog(this, "Event cancelled.");
            } else {
                JOptionPane.showMessageDialog(this, "Could not cancel the event.");
            }
            loadEvents();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Something went wrong.");
        }
    }
}