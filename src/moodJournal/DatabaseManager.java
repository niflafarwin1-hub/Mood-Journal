package moodJournal;

import java.sql.*;

/**
 * ============================================================
 *  DatabaseManager.java
 *  ------------------------------------------------------------
 *  This class handles EVERYTHING related to the database.
 *
 *  Responsibilities:
 *   - Connect to the MySQL server
 *   - Create the table if it doesn't already exist
 *   - Insert new mood entries
 *   - Fetch all saved entries
 *   - Delete an entry
 *
 *  Think of this class as the "librarian" —
 *  it knows where everything is stored and how to access it.
 * ============================================================
 */

public class DatabaseManager {
    // -------------------------------------------------------
    // DATABASE CONNECTION SETTINGS
    // Change these to match your MySQL setup!
    // -------------------------------------------------------
    private static final String DB_URL      = "jdbc:mysql://127.0.0.1:3306/mood_journal_db";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "newpass123"; // ← your password here

    // This holds our active connection to the database
    private Connection connection;

    // -------------------------------------------------------
    // CONSTRUCTOR — called when we create a DatabaseManager
    // -------------------------------------------------------
    public DatabaseManager() {
        connectToDatabase();
        createTableIfNotExists();
    }

    /**
     * Opens a connection to the MySQL database.
     * If it fails (wrong password, server not running, etc.)
     * it will print an error message.
     */
    private void connectToDatabase() {
        try {
            // Load the MySQL JDBC driver (needed to talk to MySQL)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Open the connection using our URL, username, password
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            System.out.println("✅ Connected to database successfully!");

        } catch (ClassNotFoundException e) {
            // This means the MySQL connector JAR is not added to your project
            System.err.println("❌ MySQL Driver not found! Did you add the JAR to your project?");
            e.printStackTrace();

        } catch (SQLException e) {
            // This means wrong credentials, server not running, etc.
            System.err.println("❌ Could not connect to database! Check your credentials.");
            e.printStackTrace();
        }
    }

    /**
     * Creates the 'mood_entries' table in MySQL if it doesn't exist yet.
     * This runs automatically every time the app starts — safe to call multiple times
     * because of "CREATE TABLE IF NOT EXISTS".
     *
     * Table structure:
     *   id         - auto-incrementing unique number for each entry
     *   mood       - the emoji or mood label (e.g. "😊 Happy")
     *   note       - the user's written note
     *   entry_date - the date and time the entry was saved
     */
    private void createTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS mood_entries (
                    id         INT AUTO_INCREMENT PRIMARY KEY,
                    mood       VARCHAR(50)   NOT NULL,
                    note       TEXT,
                    entry_date DATETIME      NOT NULL
                )
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Table ready!");
        } catch (SQLException e) {
            System.err.println("❌ Failed to create table.");
            e.printStackTrace();
        }
    }

    /**
     * Saves a new MoodEntry into the database.
     *
     * We use a PreparedStatement here — this is IMPORTANT!
     * PreparedStatements protect against SQL Injection attacks
     * (where someone types harmful SQL into a text field).
     *
     * @param entry The MoodEntry object to save
     * @return true if saved successfully, false otherwise
     */
    public boolean saveEntry(MoodEntry entry) {
        // The ? placeholders will be filled in safely below
        String sql = "INSERT INTO mood_entries (mood, note, entry_date) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, entry.getMood());       // fill in mood
            pstmt.setString(2, entry.getNote());       // fill in note
            pstmt.setTimestamp(3,                      // fill in date
                    Timestamp.valueOf(entry.getEntryDate()));

            pstmt.executeUpdate(); // run the INSERT
            System.out.println("✅ Entry saved!");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Failed to save entry.");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves ALL mood entries from the database,
     * ordered by newest first (DESC = descending order).
     *
     * Returns a ResultSet — think of it as a table of rows
     * that we can loop through to read each entry.
     *
     * ⚠️ NOTE: The caller is responsible for closing this ResultSet!
     *
     * @return ResultSet with all entries, or null if error
     */
    public ResultSet getAllEntries() {
        String sql = "SELECT * FROM mood_entries ORDER BY entry_date DESC";

        try {
            // We DON'T use try-with-resources here because we want
            // the ResultSet to stay open so the caller can read it
            Statement stmt = connection.createStatement();
            return stmt.executeQuery(sql);

        } catch (SQLException e) {
            System.err.println("❌ Failed to fetch entries.");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Deletes a single mood entry from the database by its ID.
     *
     * @param id The unique ID of the entry to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteEntry(int id) {
        String sql = "DELETE FROM mood_entries WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("🗑️ Entry deleted (ID: " + id + ")");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Failed to delete entry.");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Closes the database connection when we're done.
     * Always clean up connections — leaving them open wastes resources!
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔌 Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
