package flc.model;

/**
 * Represents a single booking made by a Member for a Lesson.
 * The booking tracks its status through the lifecycle:
 *   BOOKED -> ATTENDED (after attending and submitting review)
 *   BOOKED -> CHANGED  (when the member swaps to a different lesson)
 *   BOOKED -> CANCELLED (when the member cancels)
 */
public class Booking {

    private final String bookingId;
    private final Member member;
    private Lesson lesson;
    private BookingStatus status;
    private String review;
    private int rating; // 0 = not yet rated; 1–5 when attended

    public Booking(String bookingId, Member member, Lesson lesson) {
        this.bookingId = bookingId;
        this.member    = member;
        this.lesson    = lesson;
        this.status    = BookingStatus.BOOKED;
        this.review    = "";
        this.rating    = 0;
    }

    // ---- Getters ----

    public String getBookingId()      { return bookingId; }
    public Member getMember()         { return member; }
    public Lesson getLesson()         { return lesson; }
    public BookingStatus getStatus()  { return status; }
    public String getReview()         { return review; }
    public int getRating()            { return rating; }

    // ---- Setters ----

    public void setLesson(Lesson lesson)           { this.lesson = lesson; }
    public void setStatus(BookingStatus status)    { this.status = status; }
    public void setReview(String review)           { this.review = review; }
    public void setRating(int rating)              { this.rating = rating; }

    @Override
    public String toString() {
        return String.format(
            "[%s] Member: %-15s | Lesson: %s %-10s | Status: %-9s | Rating: %s",
            bookingId,
            member.getName(),
            lesson.getLessonId(),
            lesson.getExerciseType().getDisplayName(),
            status,
            rating == 0 ? "N/A" : String.valueOf(rating)
        );
    }
}
