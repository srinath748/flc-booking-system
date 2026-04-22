package flc;

import flc.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Entry point and command-line interface for the Furzefield Leisure Centre
 * booking system.
 *
 * Provides the following five functionalities:
 *   1. Book a group exercise lesson
 *   2. Change / Cancel a booking
 *   3. Attend a lesson (submit review and rating)
 *   4. Monthly lesson report
 *   5. Monthly champion exercise type report
 */
public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static FLCSystem system;

    public static void main(String[] args) {
        system = new FLCSystem();
        printBanner();

        boolean running = true;
        while (running) {
            Member member = selectMember();
            if (member == null) {
                running = false;
            } else {
                memberMenu(member);
            }
        }

        System.out.println("\nThank you for using the FLC Booking System. Goodbye!");
        sc.close();
    }

    //  Member login
    
    private static Member selectMember() {
        System.out.println("\n" + repeat("=", 50));
        System.out.println("  Select your member account");
        System.out.println(repeat("=", 50));
        List<Member> members = system.getAllMembers();
        for (int i = 0; i < members.size(); i++) {
            System.out.printf("  %2d. %s%n", i + 1, members.get(i));
        }
        System.out.println("   0. Exit system");
        System.out.print("  Your choice: ");

        int choice = readInt(0, members.size());
        if (choice == 0) return null;
        return members.get(choice - 1);
    }

    //  Main menu
    
    private static void memberMenu(Member member) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n" + repeat("=", 50));
            System.out.printf("  Main Menu — Welcome, %s!%n", member.getName());
            System.out.println(repeat("=", 50));
            System.out.println("  1. Book a group exercise lesson");
            System.out.println("  2. Change / Cancel a booking");
            System.out.println("  3. Attend a lesson");
            System.out.println("  4. Monthly lesson report");
            System.out.println("  5. Monthly champion exercise type report");
            System.out.println("  0. Log out");
            System.out.print("  Your choice: ");

            int choice = readInt(0, 5);
            switch (choice) {
                case 1: bookLesson(member); break;
                case 2: changeOrCancel(member); break;
                case 3: attendLesson(member); break;
                case 4: monthlyLessonReport(); break;
                case 5: monthlyChampionReport(); break;
                case 0: loggedIn = false; break;
            }
        }
    }

    //  Functionality 1: Book a lesson
    
    private static void bookLesson(Member member) {
        System.out.println("\n--- Book a Group Exercise Lesson ---");
        System.out.println("  How would you like to browse the timetable?");
        System.out.println("  1. By day (Saturday or Sunday)");
        System.out.println("  2. By exercise type");
        System.out.println("  0. Back");
        System.out.print("  Your choice: ");

        int choice = readInt(0, 2);
        if (choice == 0) return;

        List<Lesson> displayed = new ArrayList<>();

        if (choice == 1) {
            Day day = selectDay();
            if (day == null) return;
            displayed = system.getTimetable().getLessonsByDay(day);
        } else {
            ExerciseType type = selectExerciseType();
            if (type == null) return;
            displayed = system.getTimetable().getLessonsByExerciseType(type);
        }

        if (displayed.isEmpty()) {
            System.out.println("  No lessons found.");
            return;
        }

        printLessonList(displayed);
        Lesson lesson = promptForLesson("  Enter Lesson ID to book (or 0 to go back): ");
        if (lesson == null) return;
        system.bookLesson(member, lesson);
    }

    //  Functionality 2: Change / Cancel a booking
    
    private static void changeOrCancel(Member member) {
        System.out.println("\n--- Change / Cancel a Booking ---");
        List<Booking> active = system.getActiveBookingsForMember(member);

        if (active.isEmpty()) {
            System.out.println("  You have no active bookings to change or cancel.");
            return;
        }

        System.out.println("  Your current bookings:");
        for (int i = 0; i < active.size(); i++) {
            System.out.printf("  %2d. %s%n", i + 1, active.get(i));
        }
        System.out.println("   0. Back");
        System.out.print("  Select booking: ");

        int idx = readInt(0, active.size());
        if (idx == 0) return;
        Booking booking = active.get(idx - 1);

        System.out.println("\n  1. Change to a different lesson");
        System.out.println("  2. Cancel this booking");
        System.out.println("  0. Back");
        System.out.print("  Your choice: ");

        int action = readInt(0, 2);
        if (action == 0) return;

        if (action == 2) {
            system.cancelBooking(booking);
            return;
        }

        // Change booking — select new lesson
        System.out.println("\n  Browse lessons to change to:");
        System.out.println("  1. By day");
        System.out.println("  2. By exercise type");
        System.out.println("  0. Back");
        System.out.print("  Your choice: ");

        int browseChoice = readInt(0, 2);
        if (browseChoice == 0) return;

        List<Lesson> candidates;
        if (browseChoice == 1) {
            Day day = selectDay();
            if (day == null) return;
            candidates = system.getTimetable().getLessonsByDay(day);
        } else {
            ExerciseType type = selectExerciseType();
            if (type == null) return;
            candidates = system.getTimetable().getLessonsByExerciseType(type);
        }

        printLessonList(candidates);
        Lesson newLesson = promptForLesson("  Enter new Lesson ID (or 0 to go back): ");
        if (newLesson == null) return;
        system.changeBooking(booking, newLesson);
    }

    //  Functionality 3: Attend a lesson
    
    private static void attendLesson(Member member) {
        System.out.println("\n--- Attend a Lesson ---");
        List<Booking> attendable = system.getAttendableBookingsForMember(member);

        if (attendable.isEmpty()) {
            System.out.println("  You have no bookings available to attend.");
            return;
        }

        System.out.println("  Select the lesson you are attending:");
        for (int i = 0; i < attendable.size(); i++) {
            Booking b = attendable.get(i);
            System.out.printf("  %2d. [%s] %s — %s %s (Weekend %d)%n",
                i + 1,
                b.getBookingId(),
                b.getLesson().getExerciseType().getDisplayName(),
                b.getLesson().getDay(),
                b.getLesson().getTimeSlot().getDisplayName(),
                b.getLesson().getWeekendNumber()
            );
        }
        System.out.println("   0. Back");
        System.out.print("  Your choice: ");

        int idx = readInt(0, attendable.size());
        if (idx == 0) return;
        Booking booking = attendable.get(idx - 1);

        System.out.print("  Please write your review: ");
        String review = sc.nextLine().trim();
        if (review.isEmpty()) review = "No comment.";

        System.out.print("  Rating (1=Very dissatisfied ... 5=Very satisfied): ");
        int rating = readInt(1, 5);

        system.attendLesson(booking, review, rating);
    }

    //  Functionality 4: Monthly lesson report
   
    private static void monthlyLessonReport() {
        System.out.print("\n  Enter month number (4=April, 5=May): ");
        int month = readInt(1, 12);
        ReportGenerator.printMonthlyLessonReport(system.getTimetable(), month);
    }

    //  Functionality 5: Monthly champion report
   
    private static void monthlyChampionReport() {
        System.out.print("\n  Enter month number (4=April, 5=May): ");
        int month = readInt(1, 12);
        ReportGenerator.printMonthlyChampionReport(system.getTimetable(), month);
    }

    //  Shared UI helpers
    
    private static Day selectDay() {
        System.out.println("  1. Saturday");
        System.out.println("  2. Sunday");
        System.out.println("  0. Back");
        System.out.print("  Your choice: ");
        int choice = readInt(0, 2);
        if (choice == 0) return null;
        return choice == 1 ? Day.SATURDAY : Day.SUNDAY;
    }

    private static ExerciseType selectExerciseType() {
        ExerciseType[] types = ExerciseType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.printf("  %d. %s (£%.2f)%n", i + 1, types[i].getDisplayName(), types[i].getPrice());
        }
        System.out.println("  0. Back");
        System.out.print("  Your choice: ");
        int choice = readInt(0, types.length);
        if (choice == 0) return null;
        return types[choice - 1];
    }

    private static void printLessonList(List<Lesson> lessons) {
        System.out.println("\n  " + repeat("-", 76));
        System.out.printf("  %-6s %-12s %-10s %-22s %-7s %-8s %-6s%n",
            "ID", "Exercise", "Day", "Time", "Weekend", "Price", "Spaces");
        System.out.println("  " + repeat("-", 76));
        for (Lesson l : lessons) {
            System.out.printf("  %-6s %-12s %-10s %-22s %-7d £%-7.2f %d/%d%n",
                l.getLessonId(),
                l.getExerciseType().getDisplayName(),
                l.getDay(),
                l.getTimeSlot().getDisplayName(),
                l.getWeekendNumber(),
                l.getPrice(),
                l.getActiveBookingCount(),
                Lesson.getMaxCapacity()
            );
        }
        System.out.println("  " + repeat("-", 76));
    }

    /**
     * Prompts for a Lesson ID, looping until a valid ID is entered or the user types 0.
     * Returns the matching Lesson, or null if the user cancelled.
     */
    private static Lesson promptForLesson(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim().toUpperCase();
            if (input.equals("0")) return null;
            if (input.isEmpty()) {
                System.out.println("  Invalid input. Please enter a Lesson ID (e.g. L001) or 0 to go back.");
                continue;
            }
            Lesson lesson = system.getTimetable().getLessonById(input);
            if (lesson == null) {
                System.out.println("  [ERROR] '" + input + "' is not a valid Lesson ID. Please try again.");
            } else {
                return lesson;
            }
        }
    }

        /**
     * Reads a validated integer from stdin in the range [min, max].
     * Reprompts on invalid input.
     */
    static int readInt(int min, int max) {
        while (true) {
            try {
                String line = sc.nextLine().trim();
                int val = Integer.parseInt(line);
                if (val >= min && val <= max) return val;
                System.out.printf("  Invalid input. Please try again (enter a number %d-%d): ", min, max);
            } catch (NumberFormatException e) {
                System.out.printf("  Invalid input. Please try again (enter a number %d-%d): ", min, max);
            }
        }
    }

    private static void printBanner() {
        System.out.println(repeat("=", 50));
        System.out.println("   FURZEFIELD LEISURE CENTRE");
        System.out.println("   Group Exercise Booking System");
        System.out.println(repeat("=", 50));
    }

    private static String repeat(String s, int n) {
        return s.repeat(n);
    }
}
