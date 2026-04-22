package flc.model;

/**
 * Represents the three available time slots for lessons each day.
 */
public enum TimeSlot {
    MORNING("Morning   (09:00)"),
    AFTERNOON("Afternoon (13:00)"),
    EVENING("Evening   (18:00)");

    private final String displayName;

    TimeSlot(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
