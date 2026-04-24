package flc.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a registered member of the Furzefield Leisure Centre.
 * A member can hold multiple bookings and is identified by a unique member ID.
 *
 * Encapsulation: the internal bookings list is never exposed directly;
 * callers receive defensive copies via getBookings().
 */
public class Member {
 
    private final String memberId;
    private final String name;
    private final List<Booking> bookings;

    public Member(String memberId, String name) {
        this.memberId = memberId;
        this.name     = name;
        this.bookings = new ArrayList<>();
    }

    // ---- Getters ----

    public String getMemberId() { return memberId; }
    public String getName()     { return name; }

    /** Returns a defensive copy so callers cannot mutate internal state. */
    public List<Booking> getBookings() {
        return new ArrayList<>(bookings);
    }

    // ---- Booking management ----

    public void addBooking(Booking booking) {
        bookings.add(booking);
    }

    /**
     * Checks whether the member has a non-cancelled active booking for the
     * given lesson — prevents duplicate bookings.
     *
     * CHANGED bookings are excluded here because a CHANGED booking no longer
     * occupies the old lesson; it has been moved to a new one.
     */
    public boolean hasActiveBookingForLesson(Lesson lesson) {
        return bookings.stream()
            .anyMatch(b -> b.getLesson().equals(lesson)
                && b.getStatus() != BookingStatus.CANCELLED
                && b.getStatus() != BookingStatus.CHANGED);
    }

    /**
     * Checks for a time conflict: the member has a booking at the same
     * weekend, day, and time slot as the requested lesson.
     *
     * CHANGED bookings ARE included because after a change the booking still
     * occupies a space in the new lesson — a member cannot book a second lesson
     * at the same time as their changed booking.
     *
     * The optional {@code excludeBooking} parameter lets changeBooking() exclude
     * the booking-being-swapped from the check (it will be removed imminently),
     * preventing a false "time conflict" when the swap target has the same slot.
     *
     * @param newLesson      the lesson the member wants to enter
     * @param excludeBooking a booking to ignore during the check (may be null)
     */
    public boolean hasTimeConflict(Lesson newLesson, Booking excludeBooking) {
        return bookings.stream()
            .filter(b -> b != excludeBooking)                 // skip the booking being swapped
            .anyMatch(b -> b.getStatus() != BookingStatus.CANCELLED
                && !b.getLesson().equals(newLesson)           // different lesson
                && b.getLesson().getDay()           == newLesson.getDay()
                && b.getLesson().getTimeSlot()      == newLesson.getTimeSlot()
                && b.getLesson().getWeekendNumber() == newLesson.getWeekendNumber());
    }

    /**
     * Convenience overload — no booking excluded (used for fresh bookings).
     */
    public boolean hasTimeConflict(Lesson newLesson) {
        return hasTimeConflict(newLesson, null);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", memberId, name);
    }
}
