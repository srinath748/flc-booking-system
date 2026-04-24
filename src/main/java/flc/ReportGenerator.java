package flc;

import flc.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates the two monthly reports required by the FLC specification.
 *
 * Report 1 — Monthly Lesson Report:
 *   For each of the 24 lessons in the given month, shows:
 *     - exercise type, day, time slot
 *     - number of members who ATTENDED (not just booked)
 *     - average rating (N/A if no ratings yet)
 *
 * Report 2 — Monthly Champion Exercise Type Report:
 *   Sums income (attended members × price) per exercise type for the month.
 *   Highlights the type with the highest income.
 *
 * Design: static utility class (no instance state needed) — Single Responsibility
 * Principle: report generation is separated from system logic in FLCSystem.
 */
// Generates monthly lesson report and champion exercise report based on attendance and income
public class ReportGenerator {

    private ReportGenerator() { /* utility class — not instantiated */ }

    // =========================================================
    //  Report 1: Monthly lesson statistics
    // =========================================================

    public static void printMonthlyLessonReport(Timetable timetable, int month) {
        List<Lesson> monthLessons = timetable.getLessonsByMonth(month);
        if (monthLessons.isEmpty()) {
            System.out.println("  No lessons found for month " + month + ".");
            return;
        }

        String monthName = monthName(month);
        int totalAttended = monthLessons.stream().mapToInt(Lesson::getAttendedCount).sum();

        System.out.println("\n" + repeat("=", 84));
        System.out.printf("  MONTHLY LESSON REPORT — %s (Month %02d)%n", monthName, month);
        System.out.printf("  Total lessons: %d  |  Total attendees this month: %d%n",
            monthLessons.size(), totalAttended);
        System.out.println(repeat("=", 84));
        System.out.printf("  %-6s  %-12s  %-10s  %-22s  %-10s  %s%n",
            "ID", "Exercise", "Day", "Time Slot", "Attendees", "Avg Rating");
        System.out.println(repeat("-", 84));

        // Group by weekend for readability
        Map<Integer, List<Lesson>> byWeekend = monthLessons.stream()
            .collect(Collectors.groupingBy(Lesson::getWeekendNumber,
                TreeMap::new, Collectors.toList()));

        for (Map.Entry<Integer, List<Lesson>> entry : byWeekend.entrySet()) {
            List<Lesson> wkLessons = entry.getValue();
            int wkAttended = wkLessons.stream().mapToInt(Lesson::getAttendedCount).sum();
            System.out.printf("  --- Weekend %-2d (%s)  |  Weekend attendees: %d ---%n",
                entry.getKey(), monthName, wkAttended);

            for (Lesson l : wkLessons) {
                String avgRating = l.getAttendedCount() == 0
                    ? "N/A"
                    : String.format("%.2f / 5.00", l.getAverageRating());
                System.out.printf("  %-6s  %-12s  %-10s  %-22s  %-10d  %s%n",
                    l.getLessonId(),
                    l.getExerciseType().getDisplayName(),
                    l.getDay(),
                    l.getTimeSlot().getDisplayName(),
                    l.getAttendedCount(),
                    avgRating
                );
            }
            System.out.println(repeat(".", 84));
        }
        System.out.println(repeat("=", 84));
    }

    // =========================================================
    //  Report 2: Monthly champion exercise type by income
    // =========================================================

    public static void printMonthlyChampionReport(Timetable timetable, int month) {
        Map<ExerciseType, Double> incomeByType = getIncomeByType(timetable, month);

        if (incomeByType.isEmpty()) {
            System.out.println("  No lessons found for month " + month + ".");
            return;
        }

        ExerciseType champion = incomeByType.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);

        String monthName = monthName(month);
        System.out.println("\n" + repeat("=", 64));
        System.out.printf("  CHAMPION EXERCISE TYPE REPORT — %s (Month %02d)%n", monthName, month);
        System.out.println(repeat("=", 64));
        System.out.printf("  %-14s  %-12s  %-10s  %-12s%n",
            "Exercise Type", "Price/lesson", "Total Income", "");
        System.out.println(repeat("-", 64));

        for (Map.Entry<ExerciseType, Double> entry : incomeByType.entrySet()) {
            boolean isChampion = entry.getKey() == champion;
            String marker = isChampion ? "<-- CHAMPION" : "";
            System.out.printf("  %-14s  £%-11.2f  £%-11.2f  %s%n",
                entry.getKey().getDisplayName(),
                entry.getKey().getPrice(),
                entry.getValue(),
                marker
            );
        }

        System.out.println(repeat("-", 64));
        if (champion != null) {
            System.out.printf("  TOP EARNER: %s  |  Total income: £%.2f%n",
                champion.getDisplayName(), incomeByType.get(champion));
        }
        System.out.println(repeat("=", 64));
    }

    // =========================================================
    //  Testable helper — returns income map for a given month
    // =========================================================

    /**
     * Returns a map of ExerciseType -> total income for the given month.
     * Used both by printMonthlyChampionReport and by JUnit tests.
     *
     * Income = (number of ATTENDED bookings for that type) × price per lesson.
     */
    public static Map<ExerciseType, Double> getIncomeByType(Timetable timetable, int month) {
        List<Lesson> monthLessons = timetable.getLessonsByMonth(month);
        Map<ExerciseType, Double> income = new TreeMap<>(Comparator.comparing(Enum::name));
        for (Lesson l : monthLessons) {
            income.merge(l.getExerciseType(), l.getIncome(), Double::sum);
        }
        return income;
    }

    /**
     * Returns the ExerciseType with the highest income for the given month.
     * Useful for unit testing the champion logic directly.
     */
    public static ExerciseType getChampionType(Timetable timetable, int month) {
        Map<ExerciseType, Double> income = getIncomeByType(timetable, month);
        return income.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    // =========================================================
    //  Utility
    // =========================================================

    private static String monthName(int month) {
        String[] names = {"", "January", "February", "March", "April",
                          "May", "June", "July", "August", "September",
                          "October", "November", "December"};
        return (month >= 1 && month <= 12) ? names[month] : "Month " + month;
    }

    private static String repeat(String s, int n) {
        return s.repeat(n);
    }
}
