package com.mecca;

import com.mecca.db.DatabaseConnection;
import com.mecca.ui.auth.LoginFrame;
import com.mecca.util.KtuSeeder;
import com.mecca.util.SeedVenues;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Application entry point. OWNER: M1 ONLY. */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // fall back to default look and feel
        }
        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseConnection.getInstance(); 
                KtuSeeder.seedIfEmpty();   // KTU reference table (handbook values)
                SeedVenues.seedIfEmpty();  // campus venues// creates mecca.db and tables on first run
                new LoginFrame().setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Startup failed: " + e.getMessage(),
                        "MECCA", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
