// com/mecca/ui/student/ClaimFormPanel.java
package com.mecca.ui.student;

import com.mecca.model.KTUCategory;
import com.mecca.model.User;
import com.mecca.service.ActivityPointsService;
import com.mecca.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Form for submitting a KTU activity points claim for approval. */
public class ClaimFormPanel extends JPanel {
    private final JComboBox<KTUCategory> categoryCombo = new JComboBox<>();
    private final JComboBox<Integer> levelCombo =
            new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
    private final JTextArea noteArea = new JTextArea(6, 30);
    private final JButton submitButton = new JButton("Submit claim");

    public ClaimFormPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel heading = new JLabel("Submit KTU Points Claim");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 20f));
        add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 6, 8, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        form.add(new JLabel("Activity category"), c);

        c.gridx = 1;
        c.weightx = 1;
        categoryCombo.setRenderer(new CategoryRenderer());
        form.add(categoryCombo, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        form.add(new JLabel("Level"), c);

        c.gridx = 1;
        c.weightx = 1;
        form.add(levelCombo, c);

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Note"), c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setFont(UIManager.getFont("TextArea.font"));
        form.add(new JScrollPane(noteArea), c);

        add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(submitButton);
        add(actions, BorderLayout.SOUTH);

        submitButton.addActionListener(event -> submitClaim());
        loadCategories();
    }

    private void loadCategories() {
        try {
            List<KTUCategory> categories = ActivityPointsService.getCategories();
            categoryCombo.removeAllItems();

            if (categories != null) {
                for (KTUCategory category : categories) {
                    if (category != null) {
                        categoryCombo.addItem(category);
                    }
                }
            }

            submitButton.setEnabled(categoryCombo.getItemCount() > 0);
            if (categoryCombo.getItemCount() == 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "No activity categories are available right now.",
                        "Categories unavailable",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (Exception exception) {
            submitButton.setEnabled(false);
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load activity categories. Please try again later.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void submitClaim() {
        KTUCategory category = (KTUCategory) categoryCombo.getSelectedItem();
        Integer level = (Integer) levelCombo.getSelectedItem();

        if (category == null || level == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an activity category and level.",
                    "Missing information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        User user = AuthService.getCurrentUser();
        if (user == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please sign in before submitting a claim.",
                    "Not signed in",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String note = noteArea.getText().trim();

        try {
            boolean submitted = ActivityPointsService.submitClaim(
                    user.getUserId(),
                    category.getCode(),
                    level,
                    note
            );

            if (submitted) {
                JOptionPane.showMessageDialog(
                        this,
                        "Claim submitted for approval",
                        "Claim submitted",
                        JOptionPane.INFORMATION_MESSAGE
                );
                noteArea.setText("");
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "The claim could not be submitted. Please check your details and try again.",
                        "Claim not submitted",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not submit your claim. Please try again later.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /** Shows each category using the requested code, name, and group format. */
    private static final class CategoryRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof KTUCategory category) {
                setText(category.getCode() + " - " + category.getName()
                        + " (Group " + groupName(category.getGroupNo()) + ")");
            } else {
                setText("");
            }

            return this;
        }

        private static String groupName(int groupNo) {
            if (groupNo >= 1 && groupNo <= 3) {
                return String.valueOf((char) ('A' + groupNo - 1));
            }
            return "—";
        }
    }
}