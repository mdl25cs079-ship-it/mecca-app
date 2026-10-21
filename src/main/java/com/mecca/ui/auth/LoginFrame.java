// src/main/java/com/mecca/ui/auth/LoginFrame.java
package com.mecca.ui.auth;

import com.mecca.model.User;
import com.mecca.service.AuthService;
import com.mecca.util.SeedUsers;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.Arrays;
import java.util.Optional;

/** Login screen: branded side panel + email/password form. Owner: M2. */
public class LoginFrame extends JFrame {

    private static final Color BRAND_DARK   = new Color(0x1E3A8A);
    private static final Color BRAND_LIGHT  = new Color(0x3B82F6);
    private static final Color ACCENT       = new Color(0x2563EB);
    private static final Color ACCENT_DARK  = new Color(0x1D4ED8);
    private static final Color ERROR_RED    = new Color(0xB91C1C);
    private static final Color MUTED        = new Color(0x6B7280);
    private static final Color FIELD_BORDER = new Color(0xD1D5DB);

    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JLabel errorLabel = new JLabel(" ");

    public LoginFrame() {
        super("MECCA - Login");
        try {
            SeedUsers.seedIfEmpty();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(null,
                    "Could not prepare the database: " + e.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
        buildUi();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        SwingUtilities.invokeLater(emailField::requestFocusInWindow);
    }

    private void buildUi() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(buildBrandPanel());
        root.add(buildFormPanel());
        root.setPreferredSize(new Dimension(780, 440));
        setContentPane(root);
    }

    /** Left side: gradient background with app name and tagline. */
    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, BRAND_DARK, getWidth(), getHeight(), BRAND_LIGHT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        JLabel name = new JLabel("MECCA");
        name.setFont(name.getFont().deriveFont(Font.BOLD, 44f));
        name.setForeground(Color.WHITE);

        JLabel sub = new JLabel("MEC Campus Community App");
        sub.setFont(sub.getFont().deriveFont(Font.PLAIN, 15f));
        sub.setForeground(Color.WHITE);

        JLabel tag = new JLabel("Events  |  Clubs  |  KTU Activity Points");
        tag.setFont(tag.getFont().deriveFont(Font.PLAIN, 12f));
        tag.setForeground(new Color(0xDBEAFE));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0; panel.add(name, c);
        c.gridy = 1; c.insets = new Insets(6, 0, 0, 0); panel.add(sub, c);
        c.gridy = 2; c.insets = new Insets(28, 0, 0, 0); panel.add(tag, c);
        return panel;
    }

    /** Right side: heading, fields, show-password, error text, login button. */
    private JPanel buildFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(44, 44, 36, 44));

        JLabel heading = new JLabel("Welcome back");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 24f));
        heading.setForeground(new Color(0x111827));

        JLabel sub = new JLabel("Sign in to continue");
        sub.setForeground(MUTED);

        styleField(emailField);
        styleField(passwordField);

        char defaultEcho = passwordField.getEchoChar();
        JCheckBox showPassword = new JCheckBox("Show password");
        showPassword.setBackground(Color.WHITE);
        showPassword.setForeground(MUTED);
        showPassword.setFocusPainted(false);
        showPassword.addActionListener(e ->
                passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : defaultEcho));

        errorLabel.setForeground(ERROR_RED);

        RoundedButton loginButton = new RoundedButton("Login");
        loginButton.addActionListener(e -> attemptLogin());
        getRootPane().setDefaultButton(loginButton); // Enter key submits

        panel.add(left(heading));
        panel.add(Box.createVerticalStrut(4));
        panel.add(left(sub));
        panel.add(Box.createVerticalStrut(24));
        panel.add(left(fieldLabel("Email")));
        panel.add(Box.createVerticalStrut(4));
        panel.add(left(emailField));
        panel.add(Box.createVerticalStrut(14));
        panel.add(left(fieldLabel("Password")));
        panel.add(Box.createVerticalStrut(4));
        panel.add(left(passwordField));
        panel.add(Box.createVerticalStrut(6));
        panel.add(left(showPassword));
        panel.add(Box.createVerticalStrut(8));
        panel.add(left(errorLabel));
        panel.add(Box.createVerticalStrut(10));
        panel.add(left(loginButton));
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        l.setForeground(new Color(0x374151));
        return l;
    }

    private void styleField(JTextComponent f) {
        Border line = BorderFactory.createLineBorder(FIELD_BORDER);
        Border pad = BorderFactory.createEmptyBorder(8, 10, 8, 10);
        f.setBorder(BorderFactory.createCompoundBorder(line, pad));
        f.setFont(f.getFont().deriveFont(14f));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setPreferredSize(new Dimension(260, 40));
    }

    private <T extends JComponent> T left(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    private void attemptLogin() {
        String email = emailField.getText().trim();
        char[] chars = passwordField.getPassword();
        String password = new String(chars);
        Arrays.fill(chars, ' ');

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Enter both email and password.");
            return;
        }

        try {
            Optional<User> result = AuthService.login(email, password);
            if (result.isPresent()) {
                dispose();
                result.get().openDashboard();   // polymorphism: each role opens its dashboard
            } else {
                errorLabel.setText("Invalid email or password.");
                passwordField.setText("");
                passwordField.requestFocusInWindow();
            }
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Something went wrong while logging in. Please try again.\n" + ex.getMessage(),
                    "Login error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Flat rounded button that looks the same under any system look and feel. */
    private static class RoundedButton extends JButton {
        RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(getFont().deriveFont(Font.BOLD, 14f));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            setPreferredSize(new Dimension(260, 42));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean active = getModel().isRollover() || getModel().isPressed();
            g2.setColor(active ? ACCENT_DARK : ACCENT);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}