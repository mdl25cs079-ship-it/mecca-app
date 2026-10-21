package com.mecca.ui.auth;

import com.mecca.ui.MainFrame;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;

/** STUB login screen. Owner: M2. Replace this whole file. The dev button lets everyone test panels before auth exists. */
public class LoginFrame extends JFrame {
    public LoginFrame() {
        setTitle("MECCA - Login (STUB, M2 will replace)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 240);
        setLocationRelativeTo(null);
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("LoginFrame - TODO (M2)", SwingConstants.CENTER), BorderLayout.CENTER);
        JButton dev = new JButton("Dev: open MainFrame without login");
        dev.addActionListener(e -> {
            new MainFrame().setVisible(true);
            dispose();
        });
        p.add(dev, BorderLayout.SOUTH);
        add(p);
    }
}
