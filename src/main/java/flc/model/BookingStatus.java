package flc.model;

/**
 * Represents the lifecycle status of a booking.
 *
 * BOOKED    - lesson has been booked but not yet attended
 * ATTENDED  - member has attended the lesson and submitted a review/rating
 * CHANGED   - booking was changed to a different lesson (old booking keeps this status)
 * CANCELLED - booking was cancelled by the member
 */
public enum BookingStatus {
    BOOKED,
    ATTENDED,
    CHANGED,
    CANCELLED
}
