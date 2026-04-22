package flc;

import flc.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages the FLC timetable of 48 lessons (8 weekends × 2 days × 3 slots).
 *
 * Timetable layout (same pattern each weekend):
 *   Saturday  Morning   -> Yoga
 *   Saturday  Afternoon -> Zumba
 *   Saturday  Evening   -> Box Fit
 *   Sunday    Morning   -> Aquacise
 *   Sunday    Afternoon -> Body Blitz
 *   Sunday    Evening   -> Yoga
 *
 * Weekends 1–4 fall in month 4 (April); weekends 5–8 fall in month 5 (May).
 */
public class Timetable {

    private final List<Lesson> lessons = new ArrayList<>();

    // Exercise type assigned to each slot (same every weekend)
    private static final ExerciseType[][] SLOT_TYPES = {
        // Saturday: Morning, Afternoon, Evening
        { ExerciseType.YOGA, ExerciseType.ZUMBA, ExerciseType.BOX_FIT },
        // Sunday: Morning, Afternoon, Evening
        { ExerciseType.AQUACISE, ExerciseType.BODY_BLITZ, ExerciseType.YOGA }
    };

    private static final Day[] DAYS      = { Day.SATURDAY, Day.SUNDAY };
    private static final TimeSlot[] SLOTS = { TimeSlot.MORNING, TimeSlot.AFTERNOON, TimeSlot.EVENING };

    public Timetable() {
        buildTimetable();
    }

    private void buildTimetable() {
        int lessonCounter = 1;
        for (int weekend = 1; weekend <= 8; weekend++) {
            int month = (weekend <= 4) ? 4 : 5; // April or May
            for (int d = 0; d < DAYS.length; d++) {
                for (int s = 0; s < SLOTS.length; s++) {
                    String id = String.format("L%03d", lessonCounter++);
                    lessons.add(new Lesson(id, SLOT_TYPES[d][s], DAYS[d], SLOTS[s], weekend, month));
                }
            }
        }
    }

    // ---- Query methods ----

    public List<Lesson> getAllLessons() {
        return new ArrayList<>(lessons);
    }

    /** Get all lessons on a specific day (Saturday or Sunday) across all weekends. */
    public List<Lesson> getLessonsByDay(Day day) {
        return lessons.stream()
            .filter(l -> l.getDay() == day)
            .collect(Collectors.toList());
    }

    /** Get all lessons of a given exercise type across all weekends. */
    public List<Lesson> getLessonsByExerciseType(ExerciseType type) {
        return lessons.stream()
            .filter(l -> l.getExerciseType() == type)
            .collect(Collectors.toList());
    }

    /** Get all lessons in a given calendar month. */
    public List<Lesson> getLessonsByMonth(int month) {
        return lessons.stream()
            .filter(l -> l.getMonth() == month)
            .collect(Collectors.toList());
    }

    /** Look up a lesson by its ID string. */
    public Lesson getLessonById(String id) {
        return lessons.stream()
            .filter(l -> l.getLessonId().equalsIgnoreCase(id))
            .findFirst()
            .orElse(null);
    }
}
