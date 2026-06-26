package moodJournal;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ============================================================
 *  MoodEntry.java
 *  ------------------------------------------------------------
 *  This is the DATA MODEL — a blueprint for one mood entry.
 *
 *  In programming, a "model" represents a real-world object.
 *  Here, one MoodEntry = one diary entry the user saved.
 *
 *  It holds:
 *   - id        → unique number from the database
 *   - mood      → emoji + label like "😊 Happy"
 *   - note      → the user's written note
 *   - entryDate → date and time it was saved
 *
 *  This class uses the "POJO" pattern:
 *  Plain Old Java Object — just data + getters/setters, no logic.
 * ============================================================
 */

public class MoodEntry {
    // -------------------------------------------------------
    // FIELDS — the data stored in each entry
    // 'private' means only this class can access them directly
    // We use getters/setters below to read/write them
    // -------------------------------------------------------
    private int           id;
    private String        mood;
    private String        note;
    private LocalDateTime entryDate;

    // Formatter for displaying dates nicely, e.g. "24 Jun 2026 · 10:30 AM"
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy · hh:mm a");

    // -------------------------------------------------------
    // CONSTRUCTOR — used when creating a brand NEW entry
    // (no ID yet — the database will assign one on save)
    // -------------------------------------------------------
    public MoodEntry(String mood, String note) {
        this.mood      = mood;
        this.note      = note;
        this.entryDate = LocalDateTime.now(); // automatically use current time
    }

    // -------------------------------------------------------
    // CONSTRUCTOR — used when LOADING an entry from the database
    // (we already have the ID and date stored in MySQL)
    // -------------------------------------------------------
    public MoodEntry(int id, String mood, String note, LocalDateTime entryDate) {
        this.id        = id;
        this.mood      = mood;
        this.note      = note;
        this.entryDate = entryDate;
    }

    // -------------------------------------------------------
    // GETTERS — read the private fields from outside this class
    // -------------------------------------------------------

    /** Returns the database ID of this entry */
    public int getId() { return id; }

    /** Returns the mood string e.g. "😊 Happy" */
    public String getMood() { return mood; }

    /** Returns the written note */
    public String getNote() { return note; }

    /** Returns the date/time as a LocalDateTime object */
    public LocalDateTime getEntryDate() { return entryDate; }

    /**
     * Returns the date formatted nicely for display in the UI
     * e.g. "24 Jun 2026 · 10:30 AM"
     */
    public String getFormattedDate() {
        return entryDate.format(DISPLAY_FORMAT);
    }

    // -------------------------------------------------------
    // toString — useful for debugging (print an entry easily)
    // -------------------------------------------------------
    @Override
    public String toString() {
        return "[" + id + "] " + mood + " | " + getFormattedDate() + " | " + note;
    }
}
