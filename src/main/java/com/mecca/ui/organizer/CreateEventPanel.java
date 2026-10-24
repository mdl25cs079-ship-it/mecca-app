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
import com.mecca.util.PosterStore;
import com.mecca.util.SeedVenues;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/** Form to create an event. Also reused (in edit mode) by MyEventsPanel. */
public class CreateEventPanel extends JPanel {

    private static final Color PRIMARY = new Color(0x1E3A8A);
    private static final Color PAGE_BG = new Color(0xF4F6FA);
    private static final Color BORDER_GREY = new Color(0xD5DAE3);
    private static final Color TEXT_GREY = new Color(0x6B7280);
    private static final Font BASE = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font BOLD = new Font("Segoe UI", Font.BOLD, 12);
    private static final int POSTER_W = 220, POSTER_H = 300;

    private static class KtuItem {
        final String code;
        final String label;
        KtuItem(String code, String label) { this.code = code; this.label = label; }
        @Override public String toString() { return label; }
    }

    private final EventService eventService = new EventService();
    private final Event editing;
    private final Runnable onDone;

    private final JTextField titleField = new JTextField();
    private final JTextArea descArea = new JTextArea(4, 20);
    private final JTextField dateField =
            new JTextField(java.time.LocalDate.now().plusDays(7).toString());
    private final JTextField timeField = new JTextField("14:00");
    private final JTextField capacityField = new JTextField();
    private final JComboBox<Venue> venueBox = new JComboBox<>();
    private final JComboBox<KtuItem> ktuBox = new JComboBox<>();
    private final JComboBox<String> levelBox = new JComboBox<>(new String[]{
            "1 - College", "2 - Zonal", "3 - State", "4 - National", "5 - International"});

    private final JLabel posterLabel = new JLabel("No poster selected", SwingConstants.CENTER);
    private File chosenPoster = null;      // new poster picked in this form
    private boolean posterRemoved = false; // user pressed Remove

    public CreateEventPanel() {
        this(null, null);
    }

    public CreateEventPanel(Event toEdit, Runnable onDone) {
        this.editing = toEdit;
        this.onDone = onDone;
        try {
            SeedVenues.seedIfEmpty();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        buildUi();
        loadVenues();
        loadCategories();
        if (editing != null) fillFromEvent();
    }

    // ---------------------------------------------------------------- UI

    private void buildUi() {
        setLayout(new GridBagLayout());
        setBackground(PAGE_BG);

        JPanel card = new JPanel(new BorderLayout(0, 18));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_GREY, 1, true),
                new EmptyBorder(24, 28, 24, 28)));

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        JLabel title = new JLabel(editing == null ? "Create New Event" : "Edit Event");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(PRIMARY);
        JLabel sub = new JLabel("Fill in the details below. You can add a poster on the right.");
        sub.setFont(BASE);
        sub.setForeground(TEXT_GREY);
        header.add(title);
        header.add(sub);
        card.add(header, BorderLayout.NORTH);

        // Body: form + poster
        JPanel body = new JPanel(new BorderLayout(25, 0));
        body.setOpaque(false);
        body.add(buildForm(), BorderLayout.CENTER);
        body.add(buildPosterPanel(), BorderLayout.EAST);
        card.add(body, BorderLayout.CENTER);

        // Footer button
        JButton save = new JButton(editing == null ? "Create Event" : "Save Changes");
        save.setFont(new Font("Segoe UI", Font.BOLD, 14));
        save.setBackground(PRIMARY);
        save.setForeground(Color.WHITE);
        save.setOpaque(true);
        save.setBorderPainted(false);
        save.setFocusPainted(false);
        save.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        save.setPreferredSize(new Dimension(170, 40));
        save.addActionListener(e -> save());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(save);
        card.add(footer, BorderLayout.SOUTH);

        add(card);
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        styleText(titleField);
        styleText(dateField);
        styleText(timeField);
        styleText(capacityField);
        descArea.setFont(BASE);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(new EmptyBorder(6, 8, 6, 8));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(new LineBorder(BORDER_GREY));
        styleCombo(venueBox);
        styleCombo(ktuBox);
        styleCombo(levelBox);

        addField(form, "Title", titleField, 0, 0, 2);
        addField(form, "Description", descScroll, 1, 0, 2);
        addField(form, "Date (yyyy-MM-dd)", dateField, 2, 0, 1);
        addField(form, "Time (HH:mm)", timeField, 2, 1, 1);
        addField(form, "Capacity", capacityField, 3, 0, 1);
        addField(form, "Level", levelBox, 3, 1, 1);
        addField(form, "Venue", venueBox, 4, 0, 2);
        addField(form, "KTU Category", ktuBox, 5, 0, 2);
        return form;
    }

    private void addField(JPanel form, String label, JComponent comp, int row, int col, int span) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = col;
        gc.gridwidth = span;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(0, col == 1 ? 10 : 0, 0, 0);

        JLabel l = new JLabel(label);
        l.setFont(BOLD);
        l.setForeground(new Color(0x374151));
        gc.gridy = row * 2;
        gc.insets = new Insets(row == 0 ? 0 : 10, col == 1 ? 10 : 0, 3, 0);
        form.add(l, gc);

        gc.gridy = row * 2 + 1;
        gc.insets = new Insets(0, col == 1 ? 10 : 0, 0, 0);
        form.add(comp, gc);
    }

    private void styleText(JTextField f) {
        f.setFont(BASE);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_GREY), new EmptyBorder(7, 8, 7, 8)));
    }

    private void styleCombo(JComboBox<?> c) {
        c.setFont(BASE);
        c.setBackground(Color.WHITE);
        c.setPreferredSize(new Dimension(c.getPreferredSize().width, 34));
    }

    private JPanel buildPosterPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel l = new JLabel("Event Poster (optional)");
        l.setFont(BOLD);
        l.setForeground(new Color(0x374151));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(3));

        posterLabel.setFont(BASE);
        posterLabel.setForeground(TEXT_GREY);
        posterLabel.setOpaque(true);
        posterLabel.setBackground(PAGE_BG);
        posterLabel.setBorder(new LineBorder(BORDER_GREY));
        Dimension d = new Dimension(POSTER_W, POSTER_H);
        posterLabel.setPreferredSize(d);
        posterLabel.setMinimumSize(d);
        posterLabel.setMaximumSize(d);
        posterLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(posterLabel);
        p.add(Box.createVerticalStrut(8));

        JButton choose = new JButton("Choose Poster...");
        JButton remove = new JButton("Remove");
        choose.setFont(BASE);
        remove.setFont(BASE);
        choose.addActionListener(e -> choosePoster());
        remove.addActionListener(e -> removePoster());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btns.setOpaque(false);
        btns.add(choose);
        btns.add(Box.createHorizontalStrut(6));
        btns.add(remove);
        btns.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(btns);
        p.add(Box.createVerticalGlue());
        return p;
    }

    // ------------------------------------------------------------ Poster

    private void choosePoster() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Images (png, jpg, gif)", "png", "jpg", "jpeg", "gif"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (f.length() > 5L * 1024 * 1024) {
                JOptionPane.showMessageDialog(this, "Please choose an image under 5 MB.");
                return;
            }
            chosenPoster = f;
            posterRemoved = false;
            showPoster(f);
        }
    }

    private void removePoster() {
        chosenPoster = null;
        posterRemoved = true;
        showPoster(null);
    }

    private void showPoster(File f) {
        if (f == null) {
            posterLabel.setIcon(null);
            posterLabel.setText("No poster selected");
            return;
        }
        try {
            BufferedImage img = ImageIO.read(f);
            if (img == null) throw new Exception("Not an image");
            double s = Math.min((double) POSTER_W / img.getWidth(), (double) POSTER_H / img.getHeight());
            int w = Math.max(1, (int) (img.getWidth() * s));
            int h = Math.max(1, (int) (img.getHeight() * s));
            posterLabel.setIcon(new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH)));
            posterLabel.setText("");
        } catch (Exception ex) {
            chosenPoster = null;
            posterLabel.setIcon(null);
            posterLabel.setText("Could not read image");
        }
    }

    private void applyPosterChanges(int eventId) {
        try {
            if (chosenPoster != null) PosterStore.savePoster(eventId, chosenPoster);
            else if (posterRemoved) PosterStore.deletePoster(eventId);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Event saved, but the poster could not be stored.");
        }
    }

    // -------------------------------------------------------------- Data

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
            ex.printStackTrace();
        }
    }

    private String groupLetter(int g) {
        return g == 1 ? "A" : g == 2 ? "B" : "C";
    }

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
        showPoster(PosterStore.getPoster(editing.getEventId()));
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
            if (capacity > venue.getCapacityLimit()) {
                throw new IllegalArgumentException("Capacity is more than " + venue.getName()
                        + " allows (" + venue.getCapacityLimit() + ").");
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
            int eventId;
            if (editing != null) {
                ok = eventService.updateEvent(e);
                eventId = e.getEventId();
            } else {
                eventId = eventService.createEvent(e);
                ok = eventId > 0;
            }

            if (ok) {
                applyPosterChanges(eventId);
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
        chosenPoster = null;
        posterRemoved = false;
        showPoster(null);
    }
}