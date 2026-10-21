package com.mecca.ui.auth;

import com.mecca.service.SignUpService;
import com.mecca.util.Constants;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class RegisterDialog extends JDialog {
    private final JTextField nameField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JTextField rollField = new JTextField(20);
    private final JPasswordField passField = new JPasswordField(20);
    private final JPasswordField confirmField = new JPasswordField(20);
    private final JComboBox<String> admissionBox = new JComboBox<>(
            new String[]{Constants.ADM_REGULAR, Constants.ADM_LATERAL, Constants.ADM_PWD});

    public RegisterDialog(Frame owner) {
        super(owner, "MECCA - Student Sign Up", true);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        addRow(form, gbc, 0, "Full name", nameField);
        addRow(form, gbc, 1, "Email", emailField);
        addRow(form, gbc, 2, "Roll number", rollField);
        addRow(form, gbc, 3, "Admission type", admissionBox);
        addRow(form, gbc, 4, "Password", passField);
        addRow(form, gbc, 5, "Confirm password", confirmField);

        JButton create = new JButton("Create Account");
        JButton cancel = new JButton("Cancel");
        create.addActionListener(e -> onCreate());
        cancel.addActionListener(e -> dispose());

        JPanel buttons = new JPanel();
        buttons.add(create);
        buttons.add(cancel);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        form.add(buttons, gbc);

        getRootPane().setDefaultButton(create);
        setContentPane(form);
        pack();
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void onCreate() {
        String pass = new String(passField.getPassword());
        String confirm = new String(confirmField.getPassword());
        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Sign Up", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String error = SignUpService.registerStudent(
                nameField.getText(), emailField.getText(), pass,
                rollField.getText(), (String) admissionBox.getSelectedItem());
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Sign Up", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, "Account created. You can log in now.", "Sign Up",
                JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}