package flc;

import flc.model.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for the FLC Booking System.
 *
 * Six test cases covering all key system behaviours:
 *
 *  Test 1 — Lesson.hasSpace() / getActiveBookingCount()
 *      Capacity constraint: a 5th booking on a full lesson must be rejected.
 *
 *  Test 2 — FLCSystem.bookLesson() — duplicate booking prevention
 *      A member cannot book the same lesson twice.
 *
 *  Test 3 — FLCSystem.changeBooking() — successful change
 *      Old lesson loses one space; new lesson gains one; status becomes CHANGED.
 *
 *  Test 4 — FLCSystem.cancelBooking() — successful cancellation
 *      Booking status becomes CANCELLED and the lesson space is freed.
 *
 *  Test 5 — FLCSystem.attendLesson() — review, rating, and status recording
 *      Status becomes ATTENDED; review and rating are persisted.
 *
 *  Test 6 — ReportGenerator.getIncomeByType() / getChampionType()
 *      The champion report correctly identifies the highest-income exercise type.
 */
public class FLCSystemTest {

    private FLCSystem system;
    private Timetable timetable;

    @Before
    public void setUp() {
        system    = new FLCSystem();
        timetable = system.getTimetable();
    }

    // ---------------------------------------------------------------
    // Test 1: Lesson capacity — 5th booking rejected
    // ---------------------------------------------------------------

    @Test
    public void testLessonCapacityConstraint() {
        // Find a completely empty lesson
        Lesson lesson = timetable.getAllLessons().stream()
            .filter(l -> l.getActiveBookingCount() == 0)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No empty lesson found"));

        List<Member> members = system.getAllMembers();
        assertTrue("Need at least 5 members", members.size() >= 5);

        // Fill all 4 spaces with distinct members
        int booked = 0;
        for (Member m : members) {
            if (booked == 4) break;
            if (!m.hasActiveBookingForLesson(lesson)) {
                if (system.bookLesson(m, lesson) != null) booked++;
            }
        }

        assertFalse("Lesson should report no space after 4 bookings",
            lesson.hasSpace());
        assertEquals("Active booking count should equal max capacity (4)",
            4, lesson.getActiveBookingCount());

        // Attempt a 5th booking — must be rejected
        Member extra = members.stream()
            .filter(m -> !m.hasActiveBookingForLesson(lesson))
            .findFirst()
            .orElse(null);
        assertNotNull("Need an un-booked member to test overflow", extra);

        Booking overflow = system.bookLesson(extra, lesson);
        assertNull("5th booking on a full lesson must return null", overflow);
    }

    // ---------------------------------------------------------------
    // Test 2: No duplicate booking for the same lesson
    // ---------------------------------------------------------------

    @Test
    public void testNoDuplicateBooking() {
        Member member = system.getAllMembers().get(0); // Alice Johnson

        Lesson lesson = timetable.getAllLessons().stream()
            .filter(Lesson::hasSpace)
            .filter(l -> !member.hasActiveBookingForLesson(l))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No available lesson"));

        // First booking must succeed
        Booking first = system.bookLesson(member, lesson);
        assertNotNull("First booking should return a Booking object", first);
        assertEquals("Status should be BOOKED", BookingStatus.BOOKED, first.getStatus());

        // Second booking for the same lesson must fail
        Booking duplicate = system.bookLesson(member, lesson);
        assertNull("Duplicate booking for the same lesson should return null", duplicate);
    }

    // ---------------------------------------------------------------
    // Test 3: changeBooking — frees old slot, occupies new slot
    // ---------------------------------------------------------------

    @Test
    public void testChangeBookingUpdatesLessons() {
        Member member = system.getAllMembers().get(5); // Frank Wilson

        List<Lesson> available = timetable.getAllLessons().stream()
            .filter(Lesson::hasSpace)
            .filter(l -> !member.hasActiveBookingForLesson(l))
            .collect(Collectors.toList());

        assertTrue("Need at least 2 available lessons for this test", available.size() >= 2);

        Lesson originalLesson = available.get(0);
        Lesson newLesson      = available.get(1);

        // Make initial booking
        Booking booking = system.bookLesson(member, originalLesson);
        assertNotNull("Initial booking should succeed", booking);

        int oldCount = originalLesson.getActiveBookingCount();
        int newCount = newLesson.getActiveBookingCount();

        // Change the booking
        boolean changed = system.changeBooking(booking, newLesson);

        assertTrue("changeBooking() should return true", changed);
        assertEquals("Status should be CHANGED after swap",
            BookingStatus.CHANGED, booking.getStatus());
        assertEquals("Booking should point to the new lesson",
            newLesson, booking.getLesson());
        assertEquals("Old lesson active count should decrease by 1",
            oldCount - 1, originalLesson.getActiveBookingCount());
        assertEquals("New lesson active count should increase by 1",
            newCount + 1, newLesson.getActiveBookingCount());
    }

    // ---------------------------------------------------------------
    // Test 4: cancelBooking — frees slot and sets CANCELLED status
    // ---------------------------------------------------------------

    @Test
    public void testCancelBookingFreesSlot() {
        Member member = system.getAllMembers().get(3); // David Brown

        Lesson lesson = timetable.getAllLessons().stream()
            .filter(Lesson::hasSpace)
            .filter(l -> !member.hasActiveBookingForLesson(l))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No available lesson"));

        Booking booking = system.bookLesson(member, lesson);
        assertNotNull("Booking should succeed", booking);
        assertEquals("Initial status must be BOOKED", BookingStatus.BOOKED, booking.getStatus());

        int countBefore = lesson.getActiveBookingCount();

        // Cancel the booking
        boolean cancelled = system.cancelBooking(booking);

        assertTrue("cancelBooking() should return true", cancelled);
        assertEquals("Status should be CANCELLED",
            BookingStatus.CANCELLED, booking.getStatus());
        assertEquals("Lesson active count should decrease by 1 after cancellation",
            countBefore - 1, lesson.getActiveBookingCount());

        // Cancelling the same booking a second time must be rejected
        boolean secondCancel = system.cancelBooking(booking);
        assertFalse("Cancelling an already-cancelled booking should return false",
            secondCancel);
    }

    // ---------------------------------------------------------------
    // Test 5: attendLesson — records review, rating, status change
    // ---------------------------------------------------------------

    @Test
    public void testAttendLessonRecordsReviewAndRating() {
        Member member = system.getAllMembers().get(4); // Emma Davis

        Lesson lesson = timetable.getAllLessons().stream()
            .filter(Lesson::hasSpace)
            .filter(l -> !member.hasActiveBookingForLesson(l))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No available lesson"));

        Booking booking = system.bookLesson(member, lesson);
        assertNotNull("Setup booking should succeed", booking);
        assertEquals("Initial status should be BOOKED",
            BookingStatus.BOOKED, booking.getStatus());

        String review = "Really enjoyed the session — very well run!";
        int rating    = 5;

        boolean attended = system.attendLesson(booking, review, rating);

        assertTrue("attendLesson() should return true on success", attended);
        assertEquals("Status should change to ATTENDED",
            BookingStatus.ATTENDED, booking.getStatus());
        assertEquals("Review text should be stored correctly",
            review, booking.getReview());
        assertEquals("Rating should be stored correctly",
            rating, booking.getRating());
        assertTrue("Lesson average rating should be positive after rating submitted",
            lesson.getAverageRating() > 0);

        // Edge case: attending an already-attended booking must be rejected
        boolean reattend = system.attendLesson(booking, "again", 3);
        assertFalse("Attending an already-attended booking should return false", reattend);
    }

    // ---------------------------------------------------------------
    // Test 6: ReportGenerator — champion type has the highest income
    // ---------------------------------------------------------------

    @Test
    public void testChampionReportIdentifiesHighestIncome() {
        int month = 4; // April — has the most pre-loaded attended data

        Map<ExerciseType, Double> incomeMap =
            ReportGenerator.getIncomeByType(timetable, month);

        // Must cover at least 4 exercise types as required by the spec
        assertTrue("Income map should cover at least 4 exercise types",
            incomeMap.size() >= 4);

        // Every income value must be non-negative
        for (Map.Entry<ExerciseType, Double> entry : incomeMap.entrySet()) {
            assertTrue("Income for " + entry.getKey() + " must be >= 0",
                entry.getValue() >= 0.0);
        }

        // The champion must be the type with the single highest income
        ExerciseType champion = ReportGenerator.getChampionType(timetable, month);
        assertNotNull("Champion type must not be null for month " + month, champion);

        double championIncome = incomeMap.get(champion);
        for (Map.Entry<ExerciseType, Double> entry : incomeMap.entrySet()) {
            assertTrue(
                "Champion income (" + championIncome + ") must be >= " +
                    entry.getKey() + " income (" + entry.getValue() + ")",
                championIncome >= entry.getValue()
            );
        }
    }
}
