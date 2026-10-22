package com.mecca.ui.student;

import com.mecca.model.Event;
import com.mecca.model.User;
import com.mecca.service.AuthService;
import com.mecca.service.FeedbackService;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.HierarchyEvent;
import java.util.List;

/** Student screen to rate events they attended. Owner: M6. */
public class FeedbackPanel extends JPanel {

    private final JComboBox<Event> eventCombo = new JComboBox<>();
    private final JComboBox<Integer> ratingCombo = new JComboBox<>(new Integer[]{5, 4, 3, 2, 1});
    private final JTextArea commentsArea = new JTextArea(5, 30);
    private final JLabel status = new JLabel(" ");

    public FeedbackPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

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
        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0; g.gridy = 0; form.add(new JLabel("Event you attended"), g);
        g.gridx = 1; form.add(eventCombo, g);
        g.gridx = 0; g.gridy = 1; form.add(new JLabel("Rating (5 = best)"), g);
        g.gridx = 1; form.add(ratingCombo, g);
        g.gridx = 0; g.gridy = 2; g.anchor = GridBagConstraints.NORTHWEST; form.add(new JLabel("Comments"), g);
        g.gridx = 1; form.add(new JScrollPane(commentsArea), g);

        JButton submit = new JButton("Submit Feedback");
        submit.addActionListener(e -> onSubmit());
        g.gridx = 1; g.gridy = 3; g.anchor = GridBagConstraints.WEST; form.add(submit, g);
        g.gridx = 1; g.gridy = 4; form.add(status, g);

        JLabel heading = new JLabel("Event Feedback");
        heading.setFont(heading.getFont().deriveFont(java.awt.Font.BOLD, 20f));
        add(heading, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);

        // Reload attended events every time this tab becomes visible.
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                reloadEvents();
            }
        });
    }

    private void reloadEvents() {
        eventCombo.removeAllItems();
        User u = AuthService.getCurrentUser();
        if (u == null) {
            status.setText("Log in as a student to give feedback.");
            return;
        }
        try {
            List<Event> events = FeedbackService.getAttendedEvents(u.getUserId());
            for (Event ev : events) eventCombo.addItem(ev);
            status.setText(events.isEmpty() ? "You have not attended any event yet." : " ");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Feedback", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSubmit() {
        User u = AuthService.getCurrentUser();
        Event ev = (Event) eventCombo.getSelectedItem();
        if (u == null || ev == null) {
            JOptionPane.showMessageDialog(this, "Choose an event you attended first.", "Feedback",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if (FeedbackService.hasSubmitted(ev.getEventId(), u.getUserId())) {
                JOptionPane.showMessageDialog(this, "You already gave feedback for this event.", "Feedback",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int rating = (Integer) ratingCombo.getSelectedItem();
            boolean ok = FeedbackService.submit(ev.getEventId(), u.getUserId(), rating, commentsArea.getText());
            if (ok) {
                commentsArea.setText("");
                JOptionPane.showMessageDialog(this, "Thank you! Feedback submitted.", "Feedback",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Feedback could not be submitted. You can rate only events you attended.",
                        "Feedback", JOptionPane.WARNING_MESSAGE);
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Feedback", JOptionPane.ERROR_MESSAGE);
        }
    }
}
