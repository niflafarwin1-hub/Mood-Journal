package moodJournal;

import javax.swing.*;

/**
 * ============================================================
 *  Main.java
 *  ------------------------------------------------------------
 *  This is the ENTRY POINT of the application.
 *  Java always starts running from the main() method.
 *
 *  What happens here:
 *   1. We set a nice look-and-feel for the app
 *   2. We create the DatabaseManager (connects to MySQL)
 *   3. We launch the UI window
 *   4. When the window closes, we close the DB connection
 *
 *  This file is intentionally kept SHORT.
 *  Each class has one job — Main.java's job is just to START things.
 * ============================================================
 */

public class Main {
    public static void main(String[] args) {

        /**
         * SwingUtilities.invokeLater() ensures the UI is created
         * on the "Event Dispatch Thread" (EDT).
         *
         * Swing is NOT thread-safe, so all UI code must run on
         * the EDT. This is a standard Java Swing best practice.
         */
        SwingUtilities.invokeLater(() -> {

            // --- Step 1: Set the Look and Feel ---
            // "Nimbus" is a modern-looking theme built into Java
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                // If Nimbus isn't available, Java will use the default theme — no problem!
                System.out.println("ℹ️ Nimbus theme not available, using default.");
            }

            // --- Step 2: Connect to the database ---
            DatabaseManager db = new DatabaseManager();

            // --- Step 3: Launch the UI ---
            MoodJournalUI ui = new MoodJournalUI(db);

            // --- Step 4: Close DB connection when window closes ---
            // addWindowListener lets us run code when the window is closed
            ui.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    db.closeConnection();
                    System.out.println("👋 App closed. Goodbye!");
                }
            });
        });
    }
}
