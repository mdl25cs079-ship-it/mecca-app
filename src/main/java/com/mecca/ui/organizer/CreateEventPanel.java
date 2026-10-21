// src/main/java/com/mecca/ui/organizer/CreateEventPanel.java
package com.mecca.ui.organizer;

import com.mecca.model.Event;
import com.mecca.model.KTUCategory;
import com.mecca.model.Organizer;
import com.mecca.model.User;
import com.mecca.model.Venue;
import com.mecca.service.ActivityPointsService;
import com.mecca.service.AuthService;
import com.mecca.service.EventService;
import com.mecca.util.SeedVenues;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Form to create an event. Also reused (in edit mode) by MyEventsPanel. */
public class CreateEventPanel extends JPanel {

    // Small holder so the KTU combo can show "code - name (Group X)"
    private static class KtuItem {
        final String code;
        final String label;
        KtuItem(String code, String label) { this.code = code; this.label = label; }
        @Override public String toString() { return label; }
    }

    private final EventService eventService = new EventService();
    private final Event editing;       // null when creating a new event
    private final Runnable onDone;     // called after a successful save (may be null)

    private final JTextField titleField = new JTextField(25);
    private final JTextArea descArea = new JTextArea(3, 25);
    private final JTextField dateField = new JTextField(java.time.LocalDate.now().plusDays(7).toString(), 10);
    private final JTextField timeField = new JTextField("14:00", 6);
    private final JTextField capacityField = new JTextField(6);
    private final JComboBox<Venue> venueBox = new JComboBox<>();
    private final JComboBox<KtuItem> ktuBox = new JComboBox<>();
    private final JComboBox<String> levelBox = new JComboBox<>(new String[]{
            "1 - College", "2 - Zonal", "3 - State", "4 - National", "5 - International"});

    /** Normal use: create a new event. */
    public CreateEventPanel() {
        this(null, null);
    }

    /** Edit mode: pass the event to edit and a callback to run after saving. */
    public CreateEventPanel(Event toEdit, Runnable onDone) {
        this.editing = toEdit;
        this.onDone = onDone;
        try {
            SeedVenues.seedIfEmpty();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        buildForm();
        loadVenues();
        loadCategories();
        if (editing != null) fillFromEvent();
    }

    private void buildForm() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        descArea.setLineWrap(true);

        int row = 0;
        addRow("Title", titleField, row++);
        addRow("Description", new JScrollPane(descArea), row++);
        addRow("Date (yyyy-MM-dd)", dateField, row++);
        addRow("Time (HH:mm)", timeField, row++);
        addRow("Capacity", capacityField, row++);
        addRow("Venue", venueBox, row++);
        addRow("KTU Category", ktuBox, row++);
        addRow("Level", levelBox, row++);

        JButton save = new JButton(editing == null ? "Create Event" : "Save Changes");
        save.addActionListener(e -> save());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 1; gc.gridy = row; gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(10, 5, 5, 5);
        add(save, gc);
    }

    private void addRow(String label, JComponent comp, int row) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = row; gc.insets = new Insets(5, 5, 5, 5);
        gc.gridx = 0; gc.anchor = GridBagConstraints.EAST;
        add(new JLabel(label), gc);
        gc.gridx = 1; gc.anchor = GridBagConstraints.WEST;
        add(comp, gc);
    }

    private void loadVenues() {
        try {
            for (Venue v : eventService.getAllVenues()) venueBox.addItem(v);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadCategories() {
        ktuBox.addItem(new KtuItem(null, "(none)"));
        try {
            List<KTUCategory> cats = ActivityPointsService.getCategories();
            if (cats != null) {
                for (KTUCategory c : cats) {
                    String label = c.getCode() + " - " + c.getName()
                            + " (Group " + groupLetter(c.getGroupNo()) + ")";
                    ktuBox.addItem(new KtuItem(c.getCode(), label));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();   // categories not ready yet: dropdown just shows (none)
        }
    }

    private String groupLetter(int g) {
        return g == 1 ? "A" : g == 2 ? "B" : "C";
    }

    // Pre-fill the form when editing
    private void fillFromEvent() {
        titleField.setText(editing.getTitle());
        descArea.setText(editing.getDescription());
        String dt = editing.getDateTime();
        if (dt != null && dt.contains(" ")) {
            dateField.setText(dt.split(" ")[0]);
            timeField.setText(dt.split(" ")[1]);
        }
        capacityField.setText(String.valueOf(editing.getCapacity()));
        for (int i = 0; i < venueBox.getItemCount(); i++) {
            if (venueBox.getItemAt(i).getVenueId() == editing.getVenueId()) venueBox.setSelectedIndex(i);
        }
        for (int i = 0; i < ktuBox.getItemCount(); i++) {
            String code = ktuBox.getItemAt(i).code;
            if (code != null && code.equals(editing.getKtuCode())) ktuBox.setSelectedIndex(i);
        }
        if (editing.getLevel() >= 1 && editing.getLevel() <= 5) levelBox.setSelectedIndex(editing.getLevel() - 1);
    }

    private int currentClubId() {
        User u = AuthService.getCurrentUser();
        if (u instanceof Organizer) return ((Organizer) u).getManagedClubId();
        return -1;
    }

    private void save() {
        try {
            int clubId = currentClubId();
            if (clubId <= 0) {
                JOptionPane.showMessageDialog(this, "Your account is not linked to a club.");
                return;
            }
            Venue venue = (Venue) venueBox.getSelectedItem();
            if (venue == null) {
                JOptionPane.showMessageDialog(this, "Please choose a venue.");
                return;
            }
            int capacity;
            try {
                capacity = Integer.parseInt(capacityField.getText().trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Capacity must be a number.");
            }

            Event e = (editing != null) ? editing : new Event();
            e.setClubId(clubId);
            e.setTitle(titleField.getText().trim());
            e.setDescription(descArea.getText().trim());
            e.setDateTime(dateField.getText().trim() + " " + timeField.getText().trim());
            e.setCapacity(capacity);
            e.setVenueId(venue.getVenueId());
            e.setKtuCode(((KtuItem) ktuBox.getSelectedItem()).code);
            e.setLevel(levelBox.getSelectedIndex() + 1);

            boolean ok;
            if (editing != null) {
                ok = eventService.updateEvent(e);
            } else {
                ok = eventService.createEvent(e) > 0;
            }

            if (ok) {
                JOptionPane.showMessageDialog(this, editing == null ? "Event created!" : "Event updated!");
                if (editing == null) clearForm();
                if (onDone != null) onDone.run();
            } else {
                JOptionPane.showMessageDialog(this, "Could not save the event. Check that your club exists.");
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Check your input", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Something went wrong. Please try again.");
        }
    }

    private void clearForm() {
        titleField.setText("");
        descArea.setText("");
        capacityField.setText("");
    }
}