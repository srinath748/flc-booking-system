package flc.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a single group exercise lesson in the FLC timetable.
 * Each lesson has a fixed capacity of 4 members.
 */
public class Lesson {

    private static final int MAX_CAPACITY = 4;

    private final String lessonId;
    private final ExerciseType exerciseType;
    private final Day day;
    private final TimeSlot timeSlot;
    private final int weekendNumber; // 1–8
    private final int month;         // calendar month (4 = April, 5 = May)
    private final List<Booking> bookings;

    public Lesson(String lessonId, ExerciseType exerciseType, Day day,
                  TimeSlot timeSlot, int weekendNumber, int month) {
        this.lessonId      = lessonId;
        this.exerciseType  = exerciseType;
        this.day           = day;
        this.timeSlot      = timeSlot;
        this.weekendNumber = weekendNumber;
        this.month         = month;
        this.bookings      = new ArrayList<>();
    }

    // ---- Getters ----

    public String getLessonId()         { return lessonId; }
    public ExerciseType getExerciseType(){ return exerciseType; }
    public Day getDay()                 { return day; }
    public TimeSlot getTimeSlot()       { return timeSlot; }
    public int getWeekendNumber()       { return weekendNumber; }
    public int getMonth()               { return month; }
    public double getPrice()            { return exerciseType.getPrice(); }
    public static int getMaxCapacity()  { return MAX_CAPACITY; }

    /** Returns a defensive copy of the booking list. */
    public List<Booking> getBookings()  { return new ArrayList<>(bookings); }

    // ---- Capacity helpers ----

    /**
     * Counts bookings with status BOOKED or ATTENDED (i.e. occupying a place).
     */
    public int getActiveBookingCount() {
        return (int) bookings.stream()
            .filter(b -> b.getStatus() == BookingStatus.BOOKED
                      || b.getStatus() == BookingStatus.ATTENDED
                      || b.getStatus() == BookingStatus.CHANGED)
            .count();
    }

    public boolean hasSpace() {
        return getActiveBookingCount() < MAX_CAPACITY;
    }

    // ---- Booking management ----

    public void addBooking(Booking booking) {
        bookings.add(booking);
    }

    public void removeBooking(Booking booking) {
        bookings.remove(booking);
    }

    // ---- Statistics ----

    /** Number of members who have actually attended this lesson. */
    public int getAttendedCount() {
        return (int) bookings.stream()
            .filter(b -> b.getStatus() == BookingStatus.ATTENDED)
            .count();
    }

    /** Average rating from members who attended; returns 0.0 if no ratings yet. */
    public double getAverageRating() {
        List<Booking> rated = bookings.stream()
            .filter(b -> b.getStatus() == BookingStatus.ATTENDED && b.getRating() > 0)
            .collect(Collectors.toList());
        if (rated.isEmpty()) return 0.0;
        return rated.stream().mapToInt(Booking::getRating).average().orElse(0.0);
    }

    /** Income = attended members × lesson price. */
    public double getIncome() {
        return getAttendedCount() * exerciseType.getPrice();
    }

    // ---- Display ----

    @Override
    public String toString() {
        return String.format(
            "[%s] %-10s | %s | %s | Weekend %-2d | Month %d | £%.2f | Booked: %d/%d",
            lessonId,
            exerciseType.getDisplayName(),
            day,
            timeSlot.getDisplayName(),
            weekendNumber,
            month,
            getPrice(),
            getActiveBookingCount(),
            MAX_CAPACITY
        );
    }
}
