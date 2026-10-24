// src/main/java/com/mecca/ui/student/FeedbackPanel.java
package com.mecca.ui.student;

import com.mecca.model.Event;
import com.mecca.model.User;
import com.mecca.service.AttendanceService;
import com.mecca.service.AuthService;
import com.mecca.service.EventService;
import com.mecca.service.FeedbackService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

/** Student screen: rate an attended event and leave comments. */
public class FeedbackPanel extends JPanel {
    
    private final JComboBox<EventItem> eventCombo = new JComboBox<>();
    private final JComboBox<Integer> ratingCombo = new JComboBox<>(new Integer[] { 1, 2, 3, 4, 5 });
    private final JTextArea commentsArea = new JTextArea(5, 30);
    private final JLabel messageLabel = new JLabel(" ");
    private final JButton submitButton = new JButton("Submit");

    public FeedbackPanel() {
        ratingCombo.setSelectedItem(5);
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(2, 2, 5, 5));
        form.add(new JLabel("Event you attended:"));
        form.add(eventCombo);
        form.add(new JLabel("Rating (1-5):"));
        form.add(ratingCombo);

        JPanel north = new JPanel(new BorderLayout());
        north.add(form, BorderLayout.CENTER);
        north.add(messageLabel, BorderLayout.SOUTH);

        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);
        JScrollPane commentsScroll = new JScrollPane(commentsArea);
        commentsScroll.setBorder(BorderFactory.createTitledBorder("Comments"));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(submitButton);

        add(north, BorderLayout.NORTH);
        add(commentsScroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        submitButton.addActionListener(e -> submit());

        refresh();
    }

    /** Reloads the events this student attended (call when the tab is shown). */
    public void refresh() {
        eventCombo.removeAllItems();
        try {
            User user = AuthService.getCurrentUser();
            if (user == null) {
                messageLabel.setText("Please log in to give feedback.");
                submitButton.setEnabled(false);
                return;
            }
            List<Integer> ids = FeedbackService.getAttendedEventIds(user.getUserId());
            for (int id : ids) {
                Event ev = eventService.getEventById(id);
                if (ev != null) {
                    eventCombo.addItem(new EventItem(ev));
                }
            }
            boolean hasEvents = eventCombo.getItemCount() > 0;
            submitButton.setEnabled(hasEvents);
            messageLabel.setText(hasEvents ? " " : "You have not attended any events yet.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load your events. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Validates and saves the feedback. */
    private void submit() {
        try {
            User user = AuthService.getCurrentUser();
            EventItem item = (EventItem) eventCombo.getSelectedItem();
            if (user == null || item == null) {
                JOptionPane.showMessageDialog(this, "Please select an event first.",
                        "Missing event", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int eventId = item.event.getEventId();
            int userId = user.getUserId();
            int rating = (Integer) ratingCombo.getSelectedItem();

            if (!"PRESENT".equals(AttendanceService.getStatus(userId, eventId))) {
                JOptionPane.showMessageDialog(this, "You can only review events you attended.",
                        "Not allowed", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (FeedbackService.hasSubmitted(eventId, userId)) {
                JOptionPane.showMessageDialog(this, "You have already submitted feedback for this event.",
                        "Already submitted", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            boolean ok = FeedbackService.submit(eventId, userId, rating, commentsArea.getText());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Thank you! Your feedback was submitted.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                commentsArea.setText("");
                ratingCombo.setSelectedItem(5);
            } else {
                JOptionPane.showMessageDialog(this, "Could not submit your feedback. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Something went wrong. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
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
    private final EventService eventService = new EventService();
}