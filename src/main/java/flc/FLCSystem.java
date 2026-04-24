package flc;

import flc.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Core system controller for the Furzefield Leisure Centre booking system.
 *
 * Responsibilities:
 *   - Maintains the master lists of members, lessons (via Timetable), and bookings.
 *   - Enforces all business rules (capacity, no duplicates, no time conflicts).
 *   - Provides the five main functionalities as public methods called by Main.
 *
 * Design pattern: Facade — hides the complexity of member, lesson, and booking
 * interactions behind a single, easy-to-use interface for the CLI layer.
 */
public class FLCSystem {

    private final Timetable timetable;
    private final List<Member> members;
    private final List<Booking> allBookings;
    private int bookingCounter;

    public FLCSystem() {
        this.timetable    = new Timetable();
        this.members      = new ArrayList<>();
        this.allBookings  = new ArrayList<>();
        this.bookingCounter = 1;

        initMembers();
        initSampleData();
    }

    //  Initialisation helpers

    /** Pre-registers 10 members (assumption: members register at the front desk). */
    private void initMembers() {
        String[][] data = {
            {"M001", "Alice Johnson"},
            {"M002", "Bob Smith"},
            {"M003", "Carol White"},
            {"M004", "David Brown"},
            {"M005", "Emma Davis"},
            {"M006", "Frank Wilson"},
            {"M007", "Grace Taylor"},
            {"M008", "Henry Moore"},
            {"M009", "Isla Anderson"},
            {"M010", "Jack Thomas"}
        };
        for (String[] d : data) {
            members.add(new Member(d[0], d[1]));
        }
    }

    /**
     * Seeds the system with sample bookings, attended lessons, and reviews so
     * that the monthly reports have meaningful data to display.
     * Covers ≥ 20 reviews across ≥ 4 exercise types over 8 weekends.
     */
    private void initSampleData() {
        // --- April bookings & attended lessons (weekends 1-4, lessons L001–L024) ---
        // Weekend 1 (L001-L006)
        createAttended("M001", "L001", 5, "Fantastic yoga session, very relaxing!");
        createAttended("M002", "L001", 4, "Great instructor, will come again.");
        createAttended("M003", "L002", 3, "Zumba was fun but a bit chaotic.");
        createAttended("M004", "L002", 4, "Good energy in the class.");
        createAttended("M005", "L003", 5, "Box Fit was intense and rewarding!");
        createAttended("M006", "L004", 4, "Aquacise is my new favourite.");
        createAttended("M007", "L005", 2, "Body Blitz was too hard for me.");
        createAttended("M008", "L006", 5, "Evening yoga is the perfect end to the weekend.");

        // Weekend 2 (L007-L012)
        createAttended("M001", "L007", 4, "Another lovely yoga morning.");
        createAttended("M009", "L008", 3, "Zumba was okay, instructor different this week.");
        createAttended("M010", "L009", 5, "Box Fit is brilliant, best workout ever!");
        createAttended("M002", "L010", 4, "Aquacise really helped my joints.");
        createAttended("M003", "L011", 4, "Body Blitz getting easier each week.");

        // Weekend 3 (L013-L018)
        createAttended("M004", "L013", 5, "Yoga session was incredibly peaceful.");
        createAttended("M005", "L014", 3, "Zumba is alright, not my favourite.");
        createAttended("M006", "L015", 5, "Box Fit instructor was motivating!");
        createAttended("M007", "L016", 4, "Aquacise was wonderful.");

        // Weekend 4 (L019-L024)
        createAttended("M008", "L019", 4, "Yoga was a lovely start to the morning.");
        createAttended("M009", "L020", 5, "Best Zumba session I've had!");
        createAttended("M010", "L022", 3, "Aquacise good but pool was cold.");
        createAttended("M001", "L023", 4, "Body Blitz really tough but worth it.");
        createAttended("M002", "L024", 5, "Evening yoga perfect end to April!");

        // --- May bookings (weekends 5-8, lessons L025–L048) — some booked, some attended ---
        // Weekend 5
        createAttended("M003", "L025", 4, "Yoga in May sunshine, wonderful.");
        createAttended("M004", "L027", 5, "Box Fit getting better every week.");
        createBooked("M005", "L028");
        createBooked("M006", "L029");

        // Weekend 6
        createAttended("M007", "L031", 4, "Yoga always a highlight.");
        createBooked("M008", "L032");
        createBooked("M009", "L033");

        // Weekend 7
        createBooked("M010", "L037");
        createBooked("M001", "L038");

        // Weekend 8
        createBooked("M002", "L043");
        createBooked("M003", "L044");
        createBooked("M004", "L045");
    }

    /** Helper: create a booking and immediately mark it as attended with a review. */
    private void createAttended(String memberId, String lessonId, int rating, String review) {
        Member member = getMemberById(memberId);
        Lesson lesson = timetable.getLessonById(lessonId);
        if (member == null || lesson == null || !lesson.hasSpace()) return;

        Booking booking = new Booking(nextBookingId(), member, lesson);
        booking.setStatus(BookingStatus.ATTENDED);
        booking.setRating(rating);
        booking.setReview(review);

        lesson.addBooking(booking);
        member.addBooking(booking);
        allBookings.add(booking);
    }

    /** Helper: create a plain BOOKED booking. */
    private void createBooked(String memberId, String lessonId) {
        Member member = getMemberById(memberId);
        Lesson lesson = timetable.getLessonById(lessonId);
        if (member == null || lesson == null || !lesson.hasSpace()) return;

        Booking booking = new Booking(nextBookingId(), member, lesson);
        lesson.addBooking(booking);
        member.addBooking(booking);
        allBookings.add(booking);
    }

    private String nextBookingId() {
        return String.format("B%04d", bookingCounter++);
    }
    // Booking functionality with validation (capacity + duplicate prevention)
    //  Functionality 1: Book a lesson
    
    /**
     * Attempts to book the given lesson for the given member.
     * Enforces:
     *   - lesson capacity (max 4 active bookings)
     *   - no duplicate bookings for the same lesson
     *   - no time conflict (same weekend, day, time slot)
     *
     * @return the new Booking on success, or null with a printed error message.
     */
    public Booking bookLesson(Member member, Lesson lesson) {
        if (!lesson.hasSpace()) {
            System.out.println("  [ERROR] This lesson is fully booked (max capacity: 4 members).");
            return null;
        }
        if (member.hasActiveBookingForLesson(lesson)) {
            System.out.println("  [ERROR] You already have an active booking for this lesson.");
            return null;
        }
        if (member.hasTimeConflict(lesson)) {
            System.out.println("  [ERROR] Time conflict — you have another booking at the same weekend, day, and time slot.");
            return null;
        }

        Booking booking = new Booking(nextBookingId(), member, lesson);
        lesson.addBooking(booking);
        member.addBooking(booking);
        allBookings.add(booking);

        System.out.printf("  [SUCCESS] Booking confirmed! Booking ID: %s%n", booking.getBookingId());
        return booking;
    }

    //  Functionality 2: Change or Cancel a booking
    
    /**
     * Changes an existing booking to a different lesson.
     * The booking ID is preserved; the old lesson's place is freed.
     *
     * @return true on success.
     */
    public boolean changeBooking(Booking booking, Lesson newLesson) {
        if (booking.getStatus() == BookingStatus.ATTENDED) {
            System.out.println("  [ERROR] Cannot change a booking that has already been attended.");
            return false;
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            System.out.println("  [ERROR] Cannot change a cancelled booking.");
            return false;
        }
        if (newLesson.equals(booking.getLesson())) {
            System.out.println("  [ERROR] The new lesson is the same as the current lesson.");
            return false;
        }
        if (!newLesson.hasSpace()) {
            System.out.println("  [ERROR] The selected new lesson is fully booked (max capacity: 4 members).");
            return false;
        }
        if (booking.getMember().hasActiveBookingForLesson(newLesson)) {
            System.out.println("  [ERROR] You already have an active booking for the selected lesson.");
            return false;
        }
        // Pass the booking-being-changed as the exclusion parameter so it is
        // not counted as a conflict with itself during the swap.
        if (booking.getMember().hasTimeConflict(newLesson, booking)) {
            System.out.println("  [ERROR] Time conflict — you have another booking at the same weekend, day, and time slot.");
            return false;
        }

        // Release the old lesson spot, then commit the change
        Lesson oldLesson = booking.getLesson();
        oldLesson.removeBooking(booking);
        booking.setLesson(newLesson);
        booking.setStatus(BookingStatus.CHANGED);
        newLesson.addBooking(booking);

        System.out.printf("  [SUCCESS] Booking %s changed from %s to %s.%n",
            booking.getBookingId(),
            oldLesson.getExerciseType().getDisplayName(),
            newLesson.getExerciseType().getDisplayName());
        return true;
    }

    /**
     * Cancels an existing booking, freeing the lesson place.
     *
     * @return true on success.
     */
    public boolean cancelBooking(Booking booking) {
        if (booking.getStatus() == BookingStatus.ATTENDED) {
            System.out.println("  [ERROR] Cannot cancel a booking that has already been attended.");
            return false;
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            System.out.println("  [ERROR] Booking is already cancelled.");
            return false;
        }

        booking.getLesson().removeBooking(booking);
        booking.setStatus(BookingStatus.CANCELLED);

        System.out.printf("  [SUCCESS] Booking %s has been cancelled.%n", booking.getBookingId());
        return true;
    }

  
    //  Functionality 3: Attend a lesson (with review & rating)
  
    /**
     * Marks a booking as attended and records the member's review and rating.
     *
     * @param rating must be 1–5.
     * @return true on success.
     */
    public boolean attendLesson(Booking booking, String review, int rating) {
        if (booking.getStatus() != BookingStatus.BOOKED
                && booking.getStatus() != BookingStatus.CHANGED) {
            System.out.println("  [ERROR] You can only attend a lesson with status BOOKED or CHANGED.");
            return false;
        }
        if (rating < 1 || rating > 5) {
            System.out.println("  [ERROR] Rating must be between 1 and 5.");
            return false;
        }

        // Re-register the booking with the lesson (was removed on change; now re-add)
        Lesson lesson = booking.getLesson();
        if (!lesson.getBookings().contains(booking)) {
            lesson.addBooking(booking);
        }

        booking.setStatus(BookingStatus.ATTENDED);
        booking.setReview(review);
        booking.setRating(rating);

        System.out.printf("  [SUCCESS] Attended %s. Thank you for your review!%n",
            lesson.getExerciseType().getDisplayName());
        return true;
    }

    //  Lookup helpers used by Main
   
    public List<Member> getAllMembers() {
        return new ArrayList<>(members);
    }

    public Member getMemberById(String id) {
        return members.stream().filter(m -> m.getMemberId().equalsIgnoreCase(id))
            .findFirst().orElse(null);
    }

    public Booking getBookingById(String id) {
        return allBookings.stream().filter(b -> b.getBookingId().equalsIgnoreCase(id))
            .findFirst().orElse(null);
    }

    public Timetable getTimetable() {
        return timetable;
    }

    public List<Booking> getAllBookings() {
        return new ArrayList<>(allBookings);
    }

    /**
     * Returns all active (BOOKED or CHANGED) bookings for a member,
     * suitable for presenting the change/cancel menu.
     */
    public List<Booking> getActiveBookingsForMember(Member member) {
        return member.getBookings().stream()
            .filter(b -> b.getStatus() == BookingStatus.BOOKED
                      || b.getStatus() == BookingStatus.CHANGED)
            .collect(Collectors.toList());
    }

    /**
     * Returns all bookings a member can attend (BOOKED or CHANGED status).
     */
    public List<Booking> getAttendableBookingsForMember(Member member) {
        return member.getBookings().stream()
            .filter(b -> b.getStatus() == BookingStatus.BOOKED
                      || b.getStatus() == BookingStatus.CHANGED)
            .collect(Collectors.toList());
    }
}
