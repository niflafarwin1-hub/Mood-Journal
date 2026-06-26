package moodJournal;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * ============================================================
 *  MoodJournalUI.java
 *  ------------------------------------------------------------
 *  This is the FRONTEND — everything the user sees and clicks.
 *
 *  Built using Java Swing, which is Java's built-in GUI toolkit.
 *
 *  Layout:
 *  ┌─────────────────────────────────────┐
 *  │         🌸 Mood Journal             │  ← Header panel
 *  ├─────────────────────────────────────┤
 *  │  How are you feeling today?         │  ← Mood selector
 *  │  😊  😐  😢  😠  😴               │
 *  ├─────────────────────────────────────┤
 *  │  Write a note...  [Save Entry]      │  ← Input panel
 *  ├─────────────────────────────────────┤
 *  │  Date | Mood | Note   [Delete]      │  ← History table
 *  └─────────────────────────────────────┘
 * ============================================================
 */

public class MoodJournalUI extends JFrame{

    // -------------------------------------------------------
    // BACKEND — the database manager we'll use to save/load data
    // -------------------------------------------------------
    private final DatabaseManager db;

    // -------------------------------------------------------
    // UI COMPONENTS — declared here so all methods can access them
    // -------------------------------------------------------
    private String        selectedMood = null;   // which emoji button was clicked
    private JLabel        selectedMoodLabel;     // shows the currently selected mood
    private JTextArea     noteArea;              // where user types their note
    private JTable        historyTable;          // table showing past entries
    private DefaultTableModel tableModel;        // the data behind the table

    // -------------------------------------------------------
    // MOOD DATA — emoji + label pairs for the buttons
    // -------------------------------------------------------
    private static final String[][] MOODS = {
            {"😊", "Happy"},
            {"😐", "Okay"},
            {"😢", "Sad"},
            {"😠", "Angry"},
            {"😴", "Tired"},
            {"🤩", "Excited"},
            {"😰", "Anxious"}
    };

    // -------------------------------------------------------
    // COLORS — our app's color palette (customize freely!)
    // -------------------------------------------------------
    private static final Color COLOR_BG         = new Color(255, 248, 240); // warm cream
    private static final Color COLOR_HEADER_BG  = new Color(255, 182, 193); // soft pink
    private static final Color COLOR_ACCENT     = new Color(255, 105, 135); // rose
    private static final Color COLOR_BTN_SAVE   = new Color(100, 200, 150); // mint green
    private static final Color COLOR_BTN_DEL    = new Color(255, 100, 100); // soft red
    private static final Color COLOR_TEXT_DARK  = new Color(60,  40,  60);  // dark purple-grey
    private static final Color COLOR_TABLE_ALT  = new Color(255, 240, 245); // light pink rows

    // -------------------------------------------------------
    // CONSTRUCTOR — builds the entire window
    // -------------------------------------------------------
    public MoodJournalUI(DatabaseManager db) {
        this.db = db;

        // --- Window Settings ---
        setTitle("🌸 Mood Journal");
        setSize(800, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // center on screen
        setResizable(false);
        getContentPane().setBackground(COLOR_BG);

        // --- Build each section ---
        setLayout(new BorderLayout(0, 0));
        add(buildHeaderPanel(),  BorderLayout.NORTH);
        add(buildCenterPanel(),  BorderLayout.CENTER);
        add(buildHistoryPanel(), BorderLayout.SOUTH);

        // --- Load existing entries from DB on startup ---
        loadEntriesFromDatabase();

        // Make it visible!
        setVisible(true);
    }

    // ===========================================================
    // SECTION 1: HEADER PANEL
    // The pink banner at the top with the app title
    // ===========================================================
    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // App title
        JLabel title = new JLabel("🌸 Mood Journal", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 28));
        title.setForeground(COLOR_TEXT_DARK);

        // Subtitle
        JLabel subtitle = new JLabel("Track how you feel, one day at a time ✨", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        subtitle.setForeground(new Color(100, 60, 80));

        header.add(title,    BorderLayout.CENTER);
        header.add(subtitle, BorderLayout.SOUTH);

        return header;
    }

    // ===========================================================
    // SECTION 2: CENTER PANEL
    // Contains mood selector buttons + note input + save button
    // ===========================================================
    private JPanel buildCenterPanel() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(COLOR_BG);
        center.setBorder(BorderFactory.createEmptyBorder(15, 25, 10, 25));

        // --- Mood prompt label ---
        JLabel promptLabel = new JLabel("How are you feeling today?");
        promptLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        promptLabel.setForeground(COLOR_TEXT_DARK);
        promptLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- Mood buttons row ---
        JPanel moodButtonsPanel = buildMoodButtonsPanel();
        moodButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- Selected mood display ---
        selectedMoodLabel = new JLabel("No mood selected yet");
        selectedMoodLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        selectedMoodLabel.setForeground(Color.GRAY);
        selectedMoodLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- Note area label ---
        JLabel noteLabel = new JLabel("Write a note (optional):");
        noteLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        noteLabel.setForeground(COLOR_TEXT_DARK);
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- Text area for writing a note ---
        noteArea = new JTextArea(3, 40);
        noteArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        noteArea.setLineWrap(true);          // wrap long lines
        noteArea.setWrapStyleWord(true);     // wrap at word boundaries
        noteArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 170, 185), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        noteArea.setBackground(new Color(255, 253, 250));

        // Wrap textarea in a scroll pane (in case of long notes)
        JScrollPane noteScroll = new JScrollPane(noteArea);
        noteScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        noteScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        // --- Save button ---
        JButton saveBtn = buildStyledButton("💾  Save Entry", COLOR_BTN_SAVE);
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.addActionListener(e -> handleSaveEntry()); // calls our save method

        // --- Add everything to center panel with spacing ---
        center.add(promptLabel);
        center.add(Box.createVerticalStrut(10));
        center.add(moodButtonsPanel);
        center.add(Box.createVerticalStrut(8));
        center.add(selectedMoodLabel);
        center.add(Box.createVerticalStrut(12));
        center.add(noteLabel);
        center.add(Box.createVerticalStrut(6));
        center.add(noteScroll);
        center.add(Box.createVerticalStrut(10));
        center.add(saveBtn);

        return center;
    }

    /**
     * Builds a row of emoji mood buttons.
     * Each button, when clicked, sets the selectedMood variable.
     */
    private JPanel buildMoodButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setBackground(COLOR_BG);

        for (String[] mood : MOODS) {
            String emoji = mood[0];
            String label = mood[1];

            // Create a button with just the emoji
            JButton btn = new JButton(emoji);
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
            btn.setToolTipText(label);           // hover tooltip shows the label
            btn.setPreferredSize(new Dimension(55, 55));
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 190, 200), 2, true),
                    BorderFactory.createEmptyBorder(3, 3, 3, 3)
            ));
            btn.setBackground(Color.WHITE);

            // When clicked: remember which mood was selected
            btn.addActionListener(e -> {
                selectedMood = emoji + " " + label;
                selectedMoodLabel.setText("Selected: " + selectedMood);
                selectedMoodLabel.setForeground(COLOR_ACCENT);

                // Highlight selected button, reset others
                for (Component c : panel.getComponents()) {
                    c.setBackground(Color.WHITE);
                }
                btn.setBackground(new Color(255, 220, 230));
            });

            panel.add(btn);
        }

        return panel;
    }

    // ===========================================================
    // SECTION 3: HISTORY TABLE PANEL
    // Shows all past mood entries in a scrollable table
    // ===========================================================
    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 25, 15, 25));

        // --- Section title ---
        JLabel historyLabel = new JLabel("📖  Your Mood History");
        historyLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        historyLabel.setForeground(COLOR_TEXT_DARK);
        historyLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        // --- Table columns ---
        String[] columns = {"ID", "Date & Time", "Mood", "Note"};
        tableModel = new DefaultTableModel(columns, 0) {
            // Make all cells non-editable (read-only table)
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        historyTable.setRowHeight(30);
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        historyTable.getTableHeader().setBackground(COLOR_HEADER_BG);
        historyTable.setSelectionBackground(new Color(255, 200, 215));
        historyTable.setGridColor(new Color(230, 210, 220));
        historyTable.setShowGrid(true);

        // Alternating row colors — makes the table easier to read
        historyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {

                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, col);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : COLOR_TABLE_ALT);
                }
                return c;
            }
        });

        // Hide the ID column (we use it internally, not for display)
        historyTable.getColumnModel().getColumn(0).setMinWidth(0);
        historyTable.getColumnModel().getColumn(0).setMaxWidth(0);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(350);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setPreferredSize(new Dimension(750, 180));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 170, 185)));

        // --- Delete button ---
        JButton deleteBtn = buildStyledButton("🗑️  Delete Selected", COLOR_BTN_DEL);
        deleteBtn.addActionListener(e -> handleDeleteEntry());

        // --- Assemble ---
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomRow.setBackground(COLOR_BG);
        bottomRow.add(deleteBtn);

        panel.add(historyLabel, BorderLayout.NORTH);
        panel.add(scrollPane,   BorderLayout.CENTER);
        panel.add(bottomRow,    BorderLayout.SOUTH);

        return panel;
    }

    // ===========================================================
    // ACTION: SAVE ENTRY
    // Called when user clicks "Save Entry"
    // ===========================================================
    private void handleSaveEntry() {
        // Validate: a mood must be selected
        if (selectedMood == null) {
            showMessage("Please select a mood first! 😊", "Missing Mood", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the note text (trim removes leading/trailing spaces)
        String note = noteArea.getText().trim();

        // Create a new MoodEntry object with selected mood and note
        MoodEntry entry = new MoodEntry(selectedMood, note);

        // Save to database via DatabaseManager
        boolean success = db.saveEntry(entry);

        if (success) {
            showMessage("Entry saved! Keep journaling 🌟", "Saved", JOptionPane.INFORMATION_MESSAGE);

            // Clear inputs for the next entry
            noteArea.setText("");
            selectedMood = null;
            selectedMoodLabel.setText("No mood selected yet");
            selectedMoodLabel.setForeground(Color.GRAY);

            // Refresh the history table to show the new entry
            loadEntriesFromDatabase();
        } else {
            showMessage("Something went wrong. Check your database connection.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===========================================================
    // ACTION: DELETE ENTRY
    // Called when user clicks "Delete Selected"
    // ===========================================================
    private void handleDeleteEntry() {
        int selectedRow = historyTable.getSelectedRow();

        // Validate: a row must be selected
        if (selectedRow == -1) {
            showMessage("Please select a row to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirm before deleting (good UX practice!)
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this entry?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            // Get the hidden ID from column 0
            int id = (int) tableModel.getValueAt(selectedRow, 0);

            boolean success = db.deleteEntry(id);

            if (success) {
                tableModel.removeRow(selectedRow); // remove from table view
                showMessage("Entry deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    // ===========================================================
    // HELPER: LOAD ALL ENTRIES FROM DATABASE INTO TABLE
    // ===========================================================
    private void loadEntriesFromDatabase() {
        // Clear existing rows first
        tableModel.setRowCount(0);

        // Fetch all entries from DB
        ResultSet rs = db.getAllEntries();

        if (rs == null) return;

        try {
            while (rs.next()) {
                // Read each column from the result
                int    id        = rs.getInt("id");
                String mood      = rs.getString("mood");
                String note      = rs.getString("note");
                String dateTime  = rs.getTimestamp("entry_date").toLocalDateTime()
                        .format(java.time.format.DateTimeFormatter
                                .ofPattern("dd MMM yyyy · hh:mm a"));

                // Add a row to the table (ID is hidden but stored)
                tableModel.addRow(new Object[]{id, dateTime, mood, note});
            }

            rs.close(); // always close ResultSet when done!

        } catch (SQLException e) {
            System.err.println("❌ Error reading entries from database.");
            e.printStackTrace();
        }
    }

    // ===========================================================
    // HELPER: SHOW A POPUP MESSAGE
    // ===========================================================
    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    // ===========================================================
    // HELPER: BUILD A STYLED BUTTON (reusable)
    // ===========================================================
    private JButton buildStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 38));

        // Hover effect — darken on mouse enter
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }
}
