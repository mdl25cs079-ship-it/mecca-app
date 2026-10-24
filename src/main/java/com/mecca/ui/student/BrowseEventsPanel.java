package com.mecca.ui.student;

import com.mecca.model.Event;
import com.mecca.model.User;
import com.mecca.service.AuthService;
import com.mecca.service.EventService;
import com.mecca.service.RegistrationService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Student view: browse and search active events, and register for one.
 *
 * Contracts used:
 *   List<Event> EventService.getActiveEvents()
 *   User        AuthService.getCurrentUser()   (null if nobody is logged in)
 *   Event       getEventId(), getClubId(), getTitle(), getDateTime(), getVenueId(),
 *               getKtuCode(), getLevel()
 *   User        getUserId()
 *
 * Event only stores clubId and venueId, so the Club and Venue columns need a
 * name lookup. See clubName() and venueName() below.
 */
public class BrowseEventsPanel extends JPanel {

    private static final String[] COLUMNS =
            {"Title", "Club", "Date", "Venue", "Seats Left", "KTU Group", "Level"};

    private final RegistrationService registrationService = new RegistrationService();

    private final JTextField searchField = new JTextField(25);
    private final JButton registerButton = new JButton("Register");
    private final JButton refreshButton = new JButton("Refresh");

    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 4 ? Integer.class : String.class;
        }
    };

    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

    /** Events in the same order as the table model rows (used to find the eventId). */
    private final List<Event> events = new ArrayList<>();

    public BrowseEventsPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.add(new JLabel("Search:"));
        top.add(searchField);
        add(top, BorderLayout.NORTH);

        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.add(refreshButton);
        bottom.add(registerButton);
        bottom.setPreferredSize(new Dimension(0, 36));
        add(bottom, BorderLayout.SOUTH);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });

        registerButton.addActionListener(e -> onRegister());
        refreshButton.addActionListener(e -> loadEvents());

        loadEvents();
    }

    /** Reloads active events and recomputes seats left. */
    public void loadEvents() {
        tableModel.setRowCount(0);
        events.clear();

        List<Event> active = EventService.getActiveEvents();
        if (active == null) {
            return;
        }

        for (Event ev : active) {
            events.add(ev);
            tableModel.addRow(new Object[]{
                    ev.getTitle(),
                    clubName(ev.getClubId()),
                    ev.getDateTime(),
                    venueName(ev.getVenueId()),
                    registrationService.getSeatsLeft(ev.getEventId()),
                    ev.getKtuCode(),
                    String.valueOf(ev.getLevel())
            });
        }
        applyFilter();
    }

    /**
     * Club name for the Club column.
     * TODO: replace the fallback with the real lookup once the club service
     * contract is known, e.g.  return ClubService.getClubById(clubId).getName();
     */
    private String clubName(int clubId) {
        return "Club #" + clubId;
    }

    /**
     * Venue name for the Venue column.
     * TODO: replace the fallback with the real lookup once the venue service
     * contract is known, e.g.  return VenueService.getVenueById(venueId).getName();
     */
    private String venueName(int venueId) {
        return "Venue #" + venueId;
    }

    private void applyFilter() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
        }
    }

    private void onRegister() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an event first.",
                    "Register", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        User user = AuthService.getCurrentUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Please log in to register.",
                    "Register", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        Event event = events.get(modelRow);
        int userId = user.getUserId();
        int eventId = event.getEventId();

        if (registrationService.isAlreadyRegistered(userId, eventId)) {
            JOptionPane.showMessageDialog(this, "Already registered",
                    "Register", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (registrationService.getSeatsLeft(eventId) <= 0) {
            JOptionPane.showMessageDialog(this, "Event full",
                    "Register", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (registrationService.register(userId, eventId)) {
            JOptionPane.showMessageDialog(this, "Registered",
                    "Register", JOptionPane.INFORMATION_MESSAGE);
            loadEvents();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Registration could not be completed. Please refresh and try again.",
                    "Register", JOptionPane.ERROR_MESSAGE);
            loadEvents();
        }
    }
}
