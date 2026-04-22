# Furzefield Leisure Centre — Booking System

> **Module:** 7COM1025 Programming for Software Engineers  
> **University:** University of Hertfordshire  
> **Academic Year:** 2025/26
> **Student Id:** 25039426

A self-contained, command-line Java application for managing group exercise lesson bookings at the Furzefield Leisure Centre (FLC). Members can view timetables, book lessons, change or cancel bookings, attend sessions, and submit reviews. The system generates monthly attendance and revenue reports.

---

## Table of Contents

- [Features](#features)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Run the Program](#run-the-program)
  - [Compile from Source](#compile-from-source)
- [Usage](#usage)
- [Pre-loaded Sample Data](#pre-loaded-sample-data)
- [Timetable Overview](#timetable-overview)
- [Running the Tests](#running-the-tests)
- [Design](#design)
  - [Architecture](#architecture)
  - [Design Pattern — Facade](#design-pattern--facade)
  - [Class Overview](#class-overview)
- [Assumptions](#assumptions)
- [Technologies Used](#technologies-used)

---

## Features

| # | Functionality | Description |
|---|---|---|
| 1 | **Book a lesson** | Browse by day (Saturday/Sunday) or exercise type; capacity and conflict checks enforced |
| 2 | **Change / Cancel a booking** | Swap to a different lesson or cancel; old place released automatically |
| 3 | **Attend a lesson** | Mark attendance, write a review, and submit a rating (1–5) |
| 4 | **Monthly lesson report** | Attendee count and average rating per lesson across 4 weekends |
| 5 | **Monthly champion report** | Total income per exercise type; highlights the top earner |

**Constraint enforcement:**
- Maximum 4 members per lesson
- No duplicate bookings for the same lesson
- No time-slot conflicts (same weekend + day + time slot)
- Reviews only accepted for booked/changed lessons; cancellation blocked after attendance

---

## Project Structure

```
flc-booking-system/
├── src/
│   ├── main/java/flc/
│   │   ├── model/
│   │   │   ├── Booking.java          # Booking lifecycle and state
│   │   │   ├── BookingStatus.java    # Enum: BOOKED, ATTENDED, CHANGED, CANCELLED
│   │   │   ├── Day.java              # Enum: SATURDAY, SUNDAY
│   │   │   ├── ExerciseType.java     # Enum: YOGA, ZUMBA, AQUACISE, BOX_FIT, BODY_BLITZ
│   │   │   ├── Lesson.java           # Lesson data, capacity, income, ratings
│   │   │   ├── Member.java           # Member data, conflict and duplicate checks
│   │   │   └── TimeSlot.java         # Enum: MORNING, AFTERNOON, EVENING
│   │   ├── FLCSystem.java            # Facade — core business logic
│   │   ├── Main.java                 # CLI entry point and menus
│   │   ├── ReportGenerator.java      # Monthly report utility (static)
│   │   └── Timetable.java            # 48-lesson schedule builder and queries
│   └── test/java/flc/
│       └── FLCSystemTest.java        # 6 JUnit 4 test cases
├── flc-booking-system.jar            # Executable JAR
└── README.md
```

---

## Getting Started

### Prerequisites

- **Java 11 or higher**

```bash
java -version
```

### Run the Program

```bash
java -jar flc-booking-system.jar
```

### Compile from Source

```bash
# Create output directories
mkdir -p out/classes

# Compile all source files
find src/main/java -name "*.java" | xargs javac -d out/classes

# Run the application
java -cp out/classes flc.Main
```

**With Maven:**

```bash
mvn compile
mvn package
java -jar target/flc-booking-system.jar
```

---

## Usage

On launch, select your member account from the list. You will then see the main menu:

```
==================================================
  Main Menu — Welcome, Alice Johnson!
==================================================
  1. Book a group exercise lesson
  2. Change / Cancel a booking
  3. Attend a lesson
  4. Monthly lesson report
  5. Monthly champion exercise type report
  0. Log out
  Your choice:
```

All inputs are validated — invalid entries prompt a retry message rather than crashing or returning to the menu.

**Viewing the monthly reports** — enter `4` for the lesson report (month 4 = April) to see data from the pre-loaded sample:

```
================================================================================
  MONTHLY LESSON REPORT — April (Month 04)
  Total lessons: 24  |  Total attendees this month: 22
================================================================================
  ID      Exercise      Day         Time Slot               Attendees   Avg Rating
--------------------------------------------------------------------------------
  --- Weekend 1  (April)  |  Weekend attendees: 8 ---
  L001    Yoga          SATURDAY    Morning   (09:00)       2           4.50 / 5.00
  L002    Zumba         SATURDAY    Afternoon (13:00)       2           3.50 / 5.00
  ...
```

---

## Pre-loaded Sample Data

The system starts with the following data so both reports work immediately on launch:

| Data | Count |
|---|---|
| Pre-registered members | 10 (M001 – M010) |
| Lessons across 8 weekends | 48 |
| Attended lessons with reviews | 25 |
| Exercise types covered | 5 |

**Members:** Alice Johnson, Bob Smith, Carol White, David Brown, Emma Davis, Frank Wilson, Grace Taylor, Henry Moore, Isla Anderson, Jack Thomas

**Exercise types and prices:**

| Type | Price |
|---|---|
| Yoga | £10.00 |
| Zumba | £8.00 |
| Aquacise | £9.00 |
| Box Fit | £12.00 |
| Body Blitz | £11.00 |

---

## Timetable Overview

The same exercise pattern repeats every weekend across 8 weekends (weekends 1–4 = April, weekends 5–8 = May):

| Day | Time Slot | Exercise |
|---|---|---|
| Saturday | Morning (09:00) | Yoga |
| Saturday | Afternoon (13:00) | Zumba |
| Saturday | Evening (18:00) | Box Fit |
| Sunday | Morning (09:00) | Aquacise |
| Sunday | Afternoon (13:00) | Body Blitz |
| Sunday | Evening (18:00) | Yoga |

---

## Running the Tests

Requires `junit4.jar` and `hamcrest-core.jar` (available via `apt install junit4` on Ubuntu).

```bash
# Compile tests
javac -cp out/classes:/usr/share/java/junit4.jar:/usr/share/java/hamcrest-core.jar \
      -d out/test-classes \
      src/test/java/flc/FLCSystemTest.java

# Run tests
java -cp out/classes:out/test-classes:/usr/share/java/junit4.jar:/usr/share/java/hamcrest-core.jar \
     org.junit.runner.JUnitCore flc.FLCSystemTest
```

**Expected output:** `OK (6 tests)`

### Test Coverage

| Test | What it validates |
|---|---|
| `testLessonCapacityConstraint` | 5th booking on a full lesson is rejected |
| `testNoDuplicateBooking` | Member cannot book the same lesson twice |
| `testBookingRejectedWhenFull` | `bookLesson()` returns null when lesson is at capacity |
| `testChangeBookingUpdatesLessons` | Old slot freed; new slot occupied; status → CHANGED |
| `testAttendLessonRecordsReviewAndRating` | Review, rating, and status stored correctly; re-attendance rejected |
| `testChampionReportIdentifiesHighestIncome` | Champion type has the highest income; all incomes non-negative |

---

## Design

### Architecture

The system follows a layered architecture:

```
┌─────────────────────────────────────┐
│  Presentation Layer  (Main.java)    │  CLI menus, input validation
├─────────────────────────────────────┤
│  Facade Layer  (FLCSystem.java)     │  Business logic, constraint enforcement
├────────────────────┬────────────────┤
│  Data Layer        │  Utility Layer │
│  (Timetable.java)  │  (ReportGenerator.java)
├────────────────────┴────────────────┤
│  Model Layer                        │  Member, Lesson, Booking, enums
└─────────────────────────────────────┘
```

### Design Pattern — Facade

`FLCSystem` implements the **Facade pattern**. `Main.java` never directly manipulates model objects — it only calls methods on `FLCSystem`. This:

- Decouples the CLI from model complexity
- Makes the service layer independently unit-testable
- Allows the internal model to change without affecting the UI

`ReportGenerator` is a separate static utility class following the **Single Responsibility Principle** — report generation is a distinct concern from booking management.

### Class Overview

```
Member          — stores member data; validates duplicates and time-slot conflicts
Lesson          — stores lesson data; tracks active bookings; computes income and avg rating
Booking         — links a Member to a Lesson; tracks status through BOOKED → ATTENDED/CHANGED/CANCELLED
Timetable       — builds and queries the 48-lesson schedule
FLCSystem       — Facade; orchestrates all five user-facing operations
ReportGenerator — static utility; produces monthly lesson and champion reports
Main            — CLI; menus, input loops, output formatting
```

---

## Assumptions

- Members are pre-registered at startup; no runtime sign-up required
- No real-time clock — a lesson is "attended" when the member selects that option
- `CHANGED`-status bookings still occupy a place in the new lesson
- All data is in-memory and reset when the program exits
- No external database, authentication, or payment processing

---

## Technologies Used

- **Java 11**
- **JUnit 4** — unit testing
- **Maven** — build and dependency management