Below is the complete, step-by-step implementation guide containing the full code for every file to create or update in your Spring Boot project (`com.traininglogbook.training_logbook`).

---

## Step 1: Dependencies & Configuration

### 1.1 Update `.gitignore`

Add `/data/` to `.gitignore` so the local H2 database files are not committed to Git.

**File:** `.gitignore`

```gitignore
HELP.md
.gradle
build/
!gradle/wrapper/gradle-wrapper.jar
!**/src/main/**/build/
!**/src/test/**/build/

### STS ###
.apt_generated
.classpath
.factorypath
.project
.settings
.springBeans
.sts4-cache
bin/
!**/src/main/**/bin/
!**/src/test/**/bin/

### IntelliJ IDEA ###
.idea
*.iws
*.iml
*.ipr
out/
!**/src/main/**/out/
!**/src/test/**/out/

### NetBeans ###
/nbproject/private/
/nbbuild/
/dist/
/nbdist/
/.nb-gradle/

### VS Code ###
.vscode/

### H2 Local Database ###
/data/

```

---

### 1.2 Update `build.gradle`

Ensure `spring-boot-starter-data-jpa` and `com.h2database:h2` are included alongside `spring-boot-starter-web`.

**File:** `build.gradle`

```groovy
plugins {
	id 'java'
	id 'org.springframework.boot' version '3.4.3'
	id 'io.spring.dependency-management' version '1.1.7'
}

group = 'com.traininglogbook'
version = '0.0.1-SNAPSHOT'

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-web'
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	runtimeOnly 'com.h2database:h2'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
	testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

tasks.named('test') {
	useJUnitPlatform()
}

```

*(Note: If your machine uses Java 17 instead of 21, keep your existing `languageVersion` line in `java.toolchain`.)*

---

### 1.3 Configure `src/main/resources/application.properties`

Configure Spring Boot to persist runs to `./data/tracklog` using H2, automatically update the schema with Hibernate, and enable the H2 console at `/h2-console`.

**File:** `src/main/resources/application.properties`

```properties
spring.application.name=training-logbook

# File-backed H2 Database Configuration (persists across restarts)
spring.datasource.url=jdbc:h2:file:./data/tracklog;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA / Hibernate
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# H2 Web Console (accessible at http://localhost:8080/h2-console)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

```

---

## Step 2: Java Backend (`RunEntry`, `RunEntryRepository`, `RunEntryController`)

Create the following three files inside `src/main/java/com/traininglogbook/training_logbook/`. Keep your existing `TrainingLogbookApplication.java` and `PaceCalculatorController.java` intact so existing endpoints and tests continue to work.

### 2.1 Create `RunEntry.java`

**File:** `src/main/java/com/traininglogbook/training_logbook/RunEntry.java`

```java
package com.traininglogbook.training_logbook;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "run_entries")
public class RunEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String athleteName;

    private LocalDate runDate;

    // "AM" or "PM" to support daily doubles
    private String sessionSlot;

    private Double mileage;

    // Formatted as "MM:SS" (e.g., "6:45")
    private String pace;

    // Formatted as "MM:SS" or "HH:MM:SS" (e.g., "54:00")
    private String totalTime;

    // e.g., "Easy", "Workout", "Long Run", "Tempo", "Recovery", "Race"
    private String workoutType;

    @Column(length = 1000)
    private String notes;

    public RunEntry() {
    }

    public RunEntry(
            String athleteName,
            LocalDate runDate,
            String sessionSlot,
            Double mileage,
            String pace,
            String totalTime,
            String workoutType,
            String notes) {
        this.athleteName = athleteName;
        this.runDate = runDate;
        this.sessionSlot = sessionSlot;
        this.mileage = mileage;
        this.pace = pace;
        this.totalTime = totalTime;
        this.workoutType = workoutType;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAthleteName() {
        return athleteName;
    }

    public void setAthleteName(String athleteName) {
        this.athleteName = athleteName;
    }

    public LocalDate getRunDate() {
        return runDate;
    }

    public void setRunDate(LocalDate runDate) {
        this.runDate = runDate;
    }

    public String getSessionSlot() {
        return sessionSlot;
    }

    public void setSessionSlot(String sessionSlot) {
        this.sessionSlot = sessionSlot;
    }

    public Double getMileage() {
        return mileage;
    }

    public void setMileage(Double mileage) {
        this.mileage = mileage;
    }

    public String getPace() {
        return pace;
    }

    public void setPace(String pace) {
        this.pace = pace;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public String getWorkoutType() {
        return workoutType;
    }

    public void setWorkoutType(String workoutType) {
        this.workoutType = workoutType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

```

---

### 2.2 Create `RunEntryRepository.java`

**File:** `src/main/java/com/traininglogbook/training_logbook/RunEntryRepository.java`

```java
package com.traininglogbook.training_logbook;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RunEntryRepository extends JpaRepository<RunEntry, Long> {

    List<RunEntry> findByRunDateBetween(LocalDate startDate, LocalDate endDate);
}

```

---

### 2.3 Create `RunEntryController.java`

**File:** `src/main/java/com/traininglogbook/training_logbook/RunEntryController.java`

```java
package com.traininglogbook.training_logbook;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/runs")
public class RunEntryController {

    private final RunEntryRepository runEntryRepository;

    public RunEntryController(RunEntryRepository runEntryRepository) {
        this.runEntryRepository = runEntryRepository;
    }

    @GetMapping
    public List<RunEntry> getRuns(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<RunEntry> runs;
        if (startDate != null && endDate != null) {
            runs = runEntryRepository.findByRunDateBetween(startDate, endDate);
        } else {
            runs = runEntryRepository.findAll();
        }

        runs.sort(
                Comparator.comparing(RunEntry::getRunDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(RunEntry::getSessionSlot, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(RunEntry::getId, Comparator.nullsLast(Comparator.naturalOrder())));
        return runs;
    }

    @PostMapping
    public ResponseEntity<RunEntry> createRun(@RequestBody RunEntry runEntry) {
        if (runEntry.getRunDate() == null) {
            runEntry.setRunDate(LocalDate.now());
        }
        if (runEntry.getSessionSlot() == null || runEntry.getSessionSlot().isBlank()) {
            runEntry.setSessionSlot("AM");
        }
        if (runEntry.getWorkoutType() == null || runEntry.getWorkoutType().isBlank()) {
            runEntry.setWorkoutType("Easy");
        }
        if (runEntry.getAthleteName() == null || runEntry.getAthleteName().isBlank()) {
            runEntry.setAthleteName("Athlete");
        }
        RunEntry saved = runEntryRepository.save(runEntry);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRun(@PathVariable Long id) {
        if (!runEntryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        runEntryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

```

---

## Step 3: Main Stitch UI + Add Run Modal (`src/main/resources/static/index.html`)

Replace `src/main/resources/static/index.html` with the complete Stitch-styled logbook calendar UI below. It implements:

* **Stitch Design System**: Tailwind CSS + Material Symbols Outlined + Space Grotesk / Inter typography, sidebar navigation, week navigation controls (`Prev`, `Today`, `Next`), weekly mileage progress & summary KPI cards, and a 7-day Monday–Sunday calendar grid.
* **Interactive Day Cells & `+ Add Run` Button**: Clicking any day card (or its `+` icon, or the top `+ Add Run` button) opens the **Add Run Modal** pre-filled with that exact date (`YYYY-MM-DD`).
* **Integrated Mileage / Pace / Total Time Auto-Calculator**: Entering any two of **Mileage**, **Pace (`MM:SS`)**, or **Total Time (`MM:SS` or `HH:MM:SS`)** automatically computes the third field in real time (or when clicking the **Auto-Calculate Missing Field** button).
* **Full Persistence & Multi-Run Support**: Submitting the modal `POST`s to `/api/runs`, closes the modal, renders the run card on the target day (supporting AM/PM doubles and multiple runs per day), allows deleting individual runs via `DELETE /api/runs/{id}`, and immediately updates the weekly and daily mileage totals.

**File:** `src/main/resources/static/index.html`

```html
<!DOCTYPE html>
<html lang="en" class="dark">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>TrackLog — Team Training Logbook</title>
    <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
    <link rel="preconnect" href="https://fonts.googleapis.com" />
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Space+Grotesk:wght@600;700&display=swap" rel="stylesheet" />
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet" />
    <script>
        tailwind.config = {
            darkMode: 'class',
            theme: {
                extend: {
                    fontFamily: {
                        sans: ['Inter', 'sans-serif'],
                        display: ['Space Grotesk', 'sans-serif']
                    },
                    colors: {
                        brand: {
                            50: '#eef9ff',
                            100: '#d8f1ff',
                            400: '#38bdf8',
                            500: '#0ea5e9',
                            600: '#0284c7'
                        },
                        surface: {
                            900: '#0b0f17',
                            800: '#111827',
                            700: '#1e293b',
                            600: '#334155'
                        }
                    }
                }
            }
        };
    </script>
    <style>
        body {
            font-family: 'Inter', sans-serif;
            background-color: #0b0f17;
            color: #f8fafc;
        }
        .font-display {
            font-family: 'Space Grotesk', sans-serif;
        }
        .material-symbols-outlined {
            font-variation-settings: 'FILL' 0, 'wght' 500, 'GRAD' 0, 'opsz' 20;
        }
        /* Custom scrollbar for day columns */
        .day-runs-scroll::-webkit-scrollbar {
            width: 4px;
        }
        .day-runs-scroll::-webkit-scrollbar-track {
            background: transparent;
        }
        .day-runs-scroll::-webkit-scrollbar-thumb {
            background: #334155;
            border-radius: 9999px;
        }
    </style>
</head>
<body class="min-h-screen flex bg-[#0b0f17] text-slate-100 antialiased selection:bg-sky-500 selection:text-white">

    <!-- Sidebar Navigation (Stitch Reference Layout) -->
    <aside class="w-64 bg-[#111827] border-r border-slate-800/80 flex-col justify-between hidden lg:flex shrink-0">
        <div>
            <!-- Brand Header -->
            <div class="h-16 px-6 flex items-center gap-3 border-b border-slate-800/80">
                <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-sky-400 to-blue-600 flex items-center justify-center shadow-lg shadow-sky-500/20">
                    <span class="material-symbols-outlined text-white text-xl">sprint</span>
                </div>
                <div>
                    <h1 class="font-display font-bold text-lg tracking-tight text-white leading-none">TrackLog</h1>
                    <p class="text-[11px] text-slate-400 font-medium mt-0.5">VARSITY TRAINING HUB</p>
                </div>
            </div>

            <!-- Nav Links -->
            <nav class="p-4 space-y-1.5">
                <a href="#" class="flex items-center gap-3 px-3.5 py-2.5 rounded-xl bg-sky-500/10 text-sky-400 border border-sky-500/20 font-medium text-sm">
                    <span class="material-symbols-outlined text-[20px]">calendar_month</span>
                    Training Logbook
                </a>
                <button type="button" onclick="openAddRunModal()" class="w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-slate-400 hover:text-slate-100 hover:bg-slate-800/60 font-medium text-sm transition">
                    <span class="material-symbols-outlined text-[20px]">calculate</span>
                    Pace & Split Calculator
                </button>
                <a href="/h2-console" target="_blank" class="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-slate-400 hover:text-slate-100 hover:bg-slate-800/60 font-medium text-sm transition">
                    <span class="material-symbols-outlined text-[20px]">database</span>
                    H2 Database Console
                </a>
            </nav>

            <!-- Athlete Filter / Active Profile -->
            <div class="px-4 mt-4">
                <div class="p-4 rounded-2xl bg-slate-900/90 border border-slate-800/80 space-y-3">
                    <div class="flex items-center justify-between">
                        <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Active Athlete</span>
                        <span class="w-2 h-2 rounded-full bg-emerald-400"></span>
                    </div>
                    <input
                        id="defaultAthleteInput"
                        type="text"
                        value="Alex Rivera"
                        placeholder="Athlete Name"
                        class="w-full rounded-lg bg-slate-800/90 border-slate-700 text-sm text-white px-3 py-1.5 focus:ring-sky-500 focus:border-sky-500"
                    />
                    <div class="flex items-center justify-between text-xs text-slate-400 pt-1 border-t border-slate-800">
                        <span>Weekly Goal</span>
                        <div class="flex items-center gap-1">
                            <input
                                id="weeklyGoalInput"
                                type="number"
                                value="65"
                                min="1"
                                max="200"
                                onchange="renderCalendarAndStats()"
                                class="w-14 text-right rounded bg-slate-800 border-slate-700 text-xs text-sky-400 font-semibold py-0.5 px-1.5"
                            />
                            <span>mi</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Workout Legend -->
        <div class="p-4 border-t border-slate-800/80">
            <p class="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-3 px-2">Session Types</p>
            <div class="grid grid-cols-2 gap-2 text-xs text-slate-300 px-2">
                <div class="flex items-center gap-2"><span class="w-2.5 h-2.5 rounded-full bg-emerald-400"></span>Easy / Base</div>
                <div class="flex items-center gap-2"><span class="w-2.5 h-2.5 rounded-full bg-amber-400"></span>Workout / Track</div>
                <div class="flex items-center gap-2"><span class="w-2.5 h-2.5 rounded-full bg-purple-400"></span>Long Run</div>
                <div class="flex items-center gap-2"><span class="w-2.5 h-2.5 rounded-full bg-rose-400"></span>Tempo / Race</div>
                <div class="flex items-center gap-2"><span class="w-2.5 h-2.5 rounded-full bg-sky-400"></span>Recovery</div>
            </div>
        </div>
    </aside>

    <!-- Main Content Area -->
    <div class="flex-1 flex flex-col min-w-0">
        <!-- Top Header Bar -->
        <header class="h-16 px-6 bg-[#111827]/90 backdrop-blur border-b border-slate-800/80 flex items-center justify-between gap-4 sticky top-0 z-20">
            <div class="flex items-center gap-4">
                <div>
                    <h2 id="weekRangeHeading" class="font-display font-bold text-lg sm:text-xl text-white tracking-tight">
                        Loading Week...
                    </h2>
                    <p class="text-xs text-slate-400 hidden sm:block">Click any day card to log an AM or PM run session</p>
                </div>
            </div>

            <!-- Week Navigation & Primary CTA -->
            <div class="flex items-center gap-3">
                <div class="inline-flex items-center rounded-xl bg-slate-900 border border-slate-800 p-1">
                    <button
                        type="button"
                        onclick="shiftWeek(-1)"
                        class="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition"
                        title="Previous Week"
                    >
                        <span class="material-symbols-outlined text-lg leading-none">chevron_left</span>
                    </button>
                    <button
                        type="button"
                        onclick="goToCurrentWeek()"
                        class="px-3 py-1 text-xs font-semibold text-slate-200 hover:text-white transition"
                    >
                        Today
                    </button>
                    <button
                        type="button"
                        onclick="shiftWeek(1)"
                        class="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition"
                        title="Next Week"
                    >
                        <span class="material-symbols-outlined text-lg leading-none">chevron_right</span>
                    </button>
                </div>

                <button
                    id="addRunHeaderBtn"
                    type="button"
                    onclick="openAddRunModal()"
                    class="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-sky-500 hover:bg-sky-400 text-slate-950 font-semibold text-sm shadow-lg shadow-sky-500/20 transition cursor-pointer"
                >
                    <span class="material-symbols-outlined text-[18px]">add</span>
                    <span>+ Add Run</span>
                </button>
            </div>
        </header>

        <!-- Weekly Summary Metrics Bar -->
        <section class="px-6 pt-6 pb-2 grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
            <!-- Weekly Mileage Card -->
            <div class="p-4 rounded-2xl bg-[#111827] border border-slate-800/90 flex flex-col justify-between">
                <div class="flex items-center justify-between">
                    <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Weekly Mileage Total</span>
                    <span class="material-symbols-outlined text-sky-400">route</span>
                </div>
                <div class="mt-2 flex items-baseline gap-2">
                    <span id="weeklyMileageTotal" class="font-display font-bold text-3xl text-white">0.0</span>
                    <span class="text-sm text-slate-400 font-medium">miles</span>
                </div>
                <div class="mt-3">
                    <div class="w-full h-1.5 bg-slate-800 rounded-full overflow-hidden">
                        <div id="weeklyGoalProgress" class="h-full bg-gradient-to-r from-sky-400 to-emerald-400 transition-all duration-300" style="width: 0%"></div>
                    </div>
                    <p id="weeklyGoalSubtext" class="text-[11px] text-slate-400 mt-1.5">0% of 65 mi weekly target</p>
                </div>
            </div>

            <!-- Completed Sessions Card -->
            <div class="p-4 rounded-2xl bg-[#111827] border border-slate-800/90 flex flex-col justify-between">
                <div class="flex items-center justify-between">
                    <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Logged Sessions</span>
                    <span class="material-symbols-outlined text-emerald-400">fitness_center</span>
                </div>
                <div class="mt-2 flex items-baseline gap-2">
                    <span id="weeklySessionsCount" class="font-display font-bold text-3xl text-white">0</span>
                    <span class="text-sm text-slate-400 font-medium">runs this week</span>
                </div>
                <p id="weeklyDoublesSubtext" class="text-[11px] text-slate-400 mt-3">0 AM / 0 PM sessions</p>
            </div>

            <!-- Average Pace Card -->
            <div class="p-4 rounded-2xl bg-[#111827] border border-slate-800/90 flex flex-col justify-between">
                <div class="flex items-center justify-between">
                    <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Weekly Avg Pace</span>
                    <span class="material-symbols-outlined text-amber-400">speed</span>
                </div>
                <div class="mt-2 flex items-baseline gap-2">
                    <span id="weeklyAvgPace" class="font-display font-bold text-3xl text-white">--:--</span>
                    <span class="text-sm text-slate-400 font-medium">/ mile</span>
                </div>
                <p id="weeklyTotalTimeSubtext" class="text-[11px] text-slate-400 mt-3">Total time: 0h 00m</p>
            </div>

            <!-- Peak Day / Long Run Card -->
            <div class="p-4 rounded-2xl bg-[#111827] border border-slate-800/90 flex flex-col justify-between">
                <div class="flex items-center justify-between">
                    <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Longest Single Run</span>
                    <span class="material-symbols-outlined text-purple-400">trophy</span>
                </div>
                <div class="mt-2 flex items-baseline gap-2">
                    <span id="weeklyLongestRun" class="font-display font-bold text-3xl text-white">0.0</span>
                    <span class="text-sm text-slate-400 font-medium">miles</span>
                </div>
                <p id="weeklyWorkoutSubtext" class="text-[11px] text-slate-400 mt-3">Click any day below to add a run</p>
            </div>
        </section>

        <!-- 7-Day Logbook Calendar Grid -->
        <main class="flex-1 p-6 pt-4">
            <div id="calendarGrid" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-7 gap-3.5 min-h-[540px]">
                <!-- Populated dynamically by JavaScript -->
            </div>
        </main>
    </div>

    <!-- Add Run Modal (Stitch Dark Modal + Auto-Calculator) -->
    <div
        id="addRunModal"
        class="fixed inset-0 z-50 hidden items-center justify-center bg-slate-950/80 backdrop-blur-sm p-4 overflow-y-auto"
    >
        <div
            class="w-full max-w-xl rounded-2xl bg-[#111827] border border-slate-700/80 shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-150"
            onclick="event.stopPropagation()"
        >
            <!-- Modal Header -->
            <div class="px-6 py-4 bg-slate-900/90 border-b border-slate-800 flex items-center justify-between">
                <div class="flex items-center gap-2.5">
                    <div class="w-8 h-8 rounded-lg bg-sky-500/15 border border-sky-500/30 flex items-center justify-center text-sky-400">
                        <span class="material-symbols-outlined text-lg">directions_run</span>
                    </div>
                    <div>
                        <h3 class="font-display font-bold text-base text-white">Log Training Run</h3>
                        <p id="modalDateSubtitle" class="text-xs text-slate-400">Enter any 2 of Mileage, Pace, or Total Time to auto-calculate the 3rd</p>
                    </div>
                </div>
                <button
                    type="button"
                    onclick="closeAddRunModal()"
                    class="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition"
                >
                    <span class="material-symbols-outlined">close</span>
                </button>
            </div>

            <!-- Modal Form -->
            <form id="addRunForm" onsubmit="submitRunForm(event)" class="p-6 space-y-5">
                <!-- Row 1: Athlete, Date, Session Slot -->
                <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
                    <div>
                        <label for="athleteName" class="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
                            Athlete Name
                        </label>
                        <input
                            type="text"
                            id="athleteName"
                            name="athleteName"
                            required
                            placeholder="Alex Rivera"
                            class="w-full rounded-xl bg-slate-900 border-slate-700 text-sm text-white px-3.5 py-2 focus:ring-sky-500 focus:border-sky-500"
                        />
                    </div>

                    <div>
                        <label for="runDate" class="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
                            Run Date
                        </label>
                        <input
                            type="date"
                            id="runDate"
                            name="runDate"
                            required
                            class="w-full rounded-xl bg-slate-900 border-slate-700 text-sm text-white px-3.5 py-2 focus:ring-sky-500 focus:border-sky-500"
                        />
                    </div>

                    <div>
                        <label for="sessionSlot" class="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
                            Session (Doubles)
                        </label>
                        <select
                            id="sessionSlot"
                            name="sessionSlot"
                            class="w-full rounded-xl bg-slate-900 border-slate-700 text-sm text-white px-3.5 py-2 focus:ring-sky-500 focus:border-sky-500"
                        >
                            <option value="AM">AM — Morning Session</option>
                            <option value="PM">PM — Afternoon Session</option>
                        </select>
                    </div>
                </div>

                <!-- Row 2: Workout Type -->
                <div>
                    <label for="workoutType" class="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
                        Workout Type
                    </label>
                    <select
                        id="workoutType"
                        name="workoutType"
                        class="w-full rounded-xl bg-slate-900 border-slate-700 text-sm text-white px-3.5 py-2 focus:ring-sky-500 focus:border-sky-500"
                    >
                        <option value="Easy">Easy / Aerobic Base</option>
                        <option value="Workout">Workout / Track Intervals</option>
                        <option value="Tempo">Tempo / Threshold</option>
                        <option value="Long Run">Long Run</option>
                        <option value="Recovery">Recovery Shakeout</option>
                        <option value="Race">Race / Time Trial</option>
                    </select>
                </div>

                <!-- Row 3: Integrated Mileage / Pace / Total Time Auto-Calculator -->
                <div class="p-4 rounded-2xl bg-slate-900/90 border border-slate-800 space-y-3">
                    <div class="flex items-center justify-between">
                        <div class="flex items-center gap-1.5 text-xs font-semibold text-sky-400">
                            <span class="material-symbols-outlined text-base">calculate</span>
                            <span>Team Logbook Auto-Calculator</span>
                        </div>
                        <button
                            type="button"
                            onclick="runAutoCalculator(true)"
                            class="text-xs font-semibold px-2.5 py-1 rounded-lg bg-sky-500/15 text-sky-300 hover:bg-sky-500/25 border border-sky-500/30 transition"
                        >
                            Calculate Missing Field
                        </button>
                    </div>

                    <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
                        <div>
                            <label for="mileage" class="block text-xs font-medium text-slate-400 mb-1">
                                Mileage (mi)
                            </label>
                            <input
                                type="number"
                                id="mileage"
                                name="mileage"
                                step="0.01"
                                min="0"
                                placeholder="e.g. 8.0"
                                oninput="onCalcFieldInput('mileage')"
                                class="w-full rounded-xl bg-slate-800 border-slate-700 text-sm text-white font-semibold px-3 py-2 focus:ring-sky-500 focus:border-sky-500"
                            />
                        </div>

                        <div>
                            <label for="pace" class="block text-xs font-medium text-slate-400 mb-1">
                                Pace (MM:SS /mi)
                            </label>
                            <input
                                type="text"
                                id="pace"
                                name="pace"
                                placeholder="e.g. 6:30"
                                oninput="onCalcFieldInput('pace')"
                                class="w-full rounded-xl bg-slate-800 border-slate-700 text-sm text-white font-semibold px-3 py-2 focus:ring-sky-500 focus:border-sky-500"
                            />
                        </div>

                        <div>
                            <label for="totalTime" class="block text-xs font-medium text-slate-400 mb-1">
                                Total Time (MM:SS)
                            </label>
                            <input
                                type="text"
                                id="totalTime"
                                name="totalTime"
                                placeholder="e.g. 52:00"
                                oninput="onCalcFieldInput('totalTime')"
                                class="w-full rounded-xl bg-slate-800 border-slate-700 text-sm text-white font-semibold px-3 py-2 focus:ring-sky-500 focus:border-sky-500"
                            />
                        </div>
                    </div>

                    <p id="calcStatusText" class="text-[11px] text-slate-400">
                        Tip: Fill in any two fields (e.g., Mileage + Total Time, or Mileage + Pace) and the third updates automatically.
                    </p>
                </div>

                <!-- Row 4: Notes -->
                <div>
                    <label for="notes" class="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
                        Workout Notes & Splits
                    </label>
                    <textarea
                        id="notes"
                        name="notes"
                        rows="3"
                        placeholder="Splits, effort level, shoes, weather, how the legs felt..."
                        class="w-full rounded-xl bg-slate-900 border-slate-700 text-sm text-white px-3.5 py-2 focus:ring-sky-500 focus:border-sky-500"
                    ></textarea>
                </div>

                <!-- Modal Actions -->
                <div class="flex items-center justify-end gap-3 pt-2 border-t border-slate-800">
                    <button
                        type="button"
                        onclick="closeAddRunModal()"
                        class="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-sm font-medium transition"
                    >
                        Cancel
                    </button>
                    <button
                        type="submit"
                        id="saveRunBtn"
                        class="inline-flex items-center gap-1.5 px-5 py-2 rounded-xl bg-sky-500 hover:bg-sky-400 text-slate-950 font-semibold text-sm shadow-lg shadow-sky-500/20 transition cursor-pointer"
                    >
                        <span class="material-symbols-outlined text-[18px]">check</span>
                        <span>Save Run</span>
                    </button>
                </div>
            </form>
        </div>
    </div>

    <script>
        // --- State ---
        let currentWeekMonday = getMonday(new Date());
        let runsCache = [];
        // Track the last two calculator fields edited by the user so real-time auto-calc knows which field to solve for
        let recentCalcEdits = [];

        const DAY_NAMES = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN'];

        // --- Date Helpers ---
        function getMonday(dateObj) {
            const d = new Date(dateObj.getFullYear(), dateObj.getMonth(), dateObj.getDate());
            const day = d.getDay(); // 0 = Sun, 1 = Mon, ...
            const diff = d.getDate() - day + (day === 0 ? -6 : 1);
            d.setDate(diff);
            d.setHours(0, 0, 0, 0);
            return d;
        }

        function addDays(dateObj, days) {
            const d = new Date(dateObj.getFullYear(), dateObj.getMonth(), dateObj.getDate());
            d.setDate(d.getDate() + days);
            return d;
        }

        function toIsoDate(dateObj) {
            const y = dateObj.getFullYear();
            const m = String(dateObj.getMonth() + 1).padStart(2, '0');
            const d = String(dateObj.getDate()).padStart(2, '0');
            return `${y}-${m}-${d}`;
        }

        function formatWeekRange(monday) {
            const sunday = addDays(monday, 6);
            const optsMonthDay = { month: 'short', day: 'numeric' };
            const startStr = monday.toLocaleDateString('en-US', optsMonthDay);
            const endStr = sunday.toLocaleDateString('en-US', {
                month: 'short',
                day: 'numeric',
                year: 'numeric'
            });
            return `${startStr} – ${endStr}`;
        }

        // --- Time & Pace Auto-Calculator Logic ---
        // Parses "MM:SS" or "HH:MM:SS" into total seconds. Returns null if invalid.
        function parseDurationToSeconds(str) {
            if (!str || typeof str !== 'string') return null;
            const trimmed = str.trim().replace(/\/mi$/i, '').trim();
            if (!trimmed) return null;

            const parts = trimmed.split(':').map(p => p.trim());
            if (parts.some(p => p === '' || isNaN(p))) return null;

            if (parts.length === 2) {
                const mins = parseInt(parts[0], 10);
                const secs = parseFloat(parts[1]);
                if (mins < 0 || secs < 0 || secs >= 60) return null;
                return mins * 60 + secs;
            } else if (parts.length === 3) {
                const hrs = parseInt(parts[0], 10);
                const mins = parseInt(parts[1], 10);
                const secs = parseFloat(parts[2]);
                if (hrs < 0 || mins < 0 || mins >= 60 || secs < 0 || secs >= 60) return null;
                return hrs * 3600 + mins * 60 + secs;
            }
            return null;
        }

        // Formats seconds as "MM:SS" (or "HH:MM:SS" if >= 3600s and allowHours is true)
        function formatSeconds(totalSeconds, allowHours = false) {
            if (!isFinite(totalSeconds) || totalSeconds <= 0) return '';
            const rounded = Math.round(totalSeconds);
            if (allowHours && rounded >= 3600) {
                const hrs = Math.floor(rounded / 3600);
                const mins = Math.floor((rounded % 3600) / 60);
                const secs = rounded % 60;
                return `${hrs}:${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
            }
            const mins = Math.floor(rounded / 60);
            const secs = rounded % 60;
            return `${mins}:${String(secs).padStart(2, '0')}`;
        }

        function onCalcFieldInput(fieldName) {
            recentCalcEdits = recentCalcEdits.filter(f => f !== fieldName);
            recentCalcEdits.push(fieldName);
            if (recentCalcEdits.length > 2) {
                recentCalcEdits.shift();
            }
            runAutoCalculator(false);
        }

        function runAutoCalculator(manualTrigger = false) {
            const mileageEl = document.getElementById('mileage');
            const paceEl = document.getElementById('pace');
            const totalTimeEl = document.getElementById('totalTime');
            const statusEl = document.getElementById('calcStatusText');

            const mileageVal = parseFloat(mileageEl.value);
            const hasMileage = !isNaN(mileageVal) && mileageVal > 0;
            const paceSecs = parseDurationToSeconds(paceEl.value);
            const hasPace = paceSecs !== null && paceSecs > 0;
            const timeSecs = parseDurationToSeconds(totalTimeEl.value);
            const hasTime = timeSecs !== null && timeSecs > 0;

            // Determine which field to solve for
            let targetField = null;
            if (hasMileage && hasPace && !hasTime) {
                targetField = 'totalTime';
            } else if (hasMileage && hasTime && !hasPace) {
                targetField = 'pace';
            } else if (hasPace && hasTime && !hasMileage) {
                targetField = 'mileage';
            } else if (recentCalcEdits.length === 2) {
                const allFields = ['mileage', 'pace', 'totalTime'];
                targetField = allFields.find(f => !recentCalcEdits.includes(f));
            }

            if (targetField === 'totalTime' && hasMileage && hasPace) {
                const computedTimeSecs = mileageVal * paceSecs;
                totalTimeEl.value = formatSeconds(computedTimeSecs, true);
                statusEl.textContent = `Auto-calculated Total Time: ${totalTimeEl.value} (${mileageVal} mi @ ${paceEl.value}/mi)`;
                statusEl.className = 'text-[11px] text-emerald-400';
            } else if (targetField === 'pace' && hasMileage && hasTime) {
                const computedPaceSecs = timeSecs / mileageVal;
                paceEl.value = formatSeconds(computedPaceSecs, false);
                statusEl.textContent = `Auto-calculated Pace: ${paceEl.value}/mi (${mileageVal} mi in ${totalTimeEl.value})`;
                statusEl.className = 'text-[11px] text-emerald-400';
            } else if (targetField === 'mileage' && hasPace && hasTime) {
                const computedMiles = timeSecs / paceSecs;
                mileageEl.value = computedMiles.toFixed(2);
                statusEl.textContent = `Auto-calculated Mileage: ${mileageEl.value} mi (${totalTimeEl.value} @ ${paceEl.value}/mi)`;
                statusEl.className = 'text-[11px] text-emerald-400';
            } else if (manualTrigger) {
                statusEl.textContent = 'Enter any 2 valid fields (Mileage, Pace as MM:SS, or Total Time as MM:SS) to calculate the 3rd.';
                statusEl.className = 'text-[11px] text-amber-400';
            }
        }

        // --- API Integration ---
        async function fetchRunsForCurrentWeek() {
            const startStr = toIsoDate(currentWeekMonday);
            const endStr = toIsoDate(addDays(currentWeekMonday, 6));
            try {
                const response = await fetch(`/api/runs?startDate=${startStr}&endDate=${endStr}`);
                if (!response.ok) throw new Error('Failed to load runs');
                runsCache = await response.json();
                renderCalendarAndStats();
            } catch (err) {
                console.error('Error fetching runs:', err);
                renderCalendarAndStats();
            }
        }

        async function submitRunForm(event) {
            event.preventDefault();
            // Ensure any missing 3rd calculator field is computed before saving
            runAutoCalculator(false);

            const athleteName = document.getElementById('athleteName').value.trim() || 'Athlete';
            const runDate = document.getElementById('runDate').value;
            const sessionSlot = document.getElementById('sessionSlot').value;
            const workoutType = document.getElementById('workoutType').value;
            const mileageRaw = parseFloat(document.getElementById('mileage').value);
            const mileage = isNaN(mileageRaw) ? 0.0 : mileageRaw;
            const pace = document.getElementById('pace').value.trim();
            const totalTime = document.getElementById('totalTime').value.trim();
            const notes = document.getElementById('notes').value.trim();

            const payload = {
                athleteName,
                runDate,
                sessionSlot,
                mileage,
                pace,
                totalTime,
                workoutType,
                notes
            };

            try {
                const response = await fetch('/api/runs', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
                if (!response.ok) throw new Error('Failed to save run');

                closeAddRunModal();

                // If the user logged a run for a date outside the currently viewed week, jump to that week
                const [y, m, d] = runDate.split('-').map(Number);
                const savedDateObj = new Date(y, m - 1, d);
                currentWeekMonday = getMonday(savedDateObj);

                await fetchRunsForCurrentWeek();
            } catch (err) {
                console.error('Error saving run:', err);
                alert('Could not save run entry. Check server logs.');
            }
        }

        async function deleteRun(event, runId) {
            event.stopPropagation();
            try {
                const response = await fetch(`/api/runs/${runId}`, {
                    method: 'DELETE'
                });
                if (response.ok) {
                    await fetchRunsForCurrentWeek();
                }
            } catch (err) {
                console.error('Error deleting run:', err);
            }
        }

        // --- Workout Badge Styling ---
        function getWorkoutStyle(workoutType) {
            const type = (workoutType || 'Easy').toLowerCase();
            if (type.includes('workout') || type.includes('track') || type.includes('interval')) {
                return {
                    badge: 'bg-amber-500/15 text-amber-300 border-amber-500/30',
                    border: 'border-l-amber-400'
                };
            }
            if (type.includes('long')) {
                return {
                    badge: 'bg-purple-500/15 text-purple-300 border-purple-500/30',
                    border: 'border-l-purple-400'
                };
            }
            if (type.includes('tempo') || type.includes('race')) {
                return {
                    badge: 'bg-rose-500/15 text-rose-300 border-rose-500/30',
                    border: 'border-l-rose-400'
                };
            }
            if (type.includes('recovery')) {
                return {
                    badge: 'bg-sky-500/15 text-sky-300 border-sky-500/30',
                    border: 'border-l-sky-400'
                };
            }
            return {
                badge: 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30',
                border: 'border-l-emerald-400'
            };
        }

        // --- Render UI ---
        function renderCalendarAndStats() {
            document.getElementById('weekRangeHeading').textContent = formatWeekRange(currentWeekMonday);

            const grid = document.getElementById('calendarGrid');
            grid.innerHTML = '';

            const todayIso = toIsoDate(new Date());
            let weeklyMileage = 0;
            let totalSecondsAllRuns = 0;
            let mileageWithTime = 0;
            let longestRun = 0;
            let amCount = 0;
            let pmCount = 0;

            for (let i = 0; i < 7; i++) {
                const dayDate = addDays(currentWeekMonday, i);
                const dayIso = toIsoDate(dayDate);
                const isToday = dayIso === todayIso;
                const dayRuns = runsCache.filter(r => r.runDate === dayIso);

                const dayMileage = dayRuns.reduce((sum, r) => sum + (Number(r.mileage) || 0), 0);
                weeklyMileage += dayMileage;

                dayRuns.forEach(r => {
                    const m = Number(r.mileage) || 0;
                    if (m > longestRun) longestRun = m;
                    if ((r.sessionSlot || 'AM').toUpperCase() === 'PM') {
                        pmCount++;
                    } else {
                        amCount++;
                    }
                    const secs = parseDurationToSeconds(r.totalTime);
                    if (secs && m > 0) {
                        totalSecondsAllRuns += secs;
                        mileageWithTime += m;
                    }
                });

                // Build Day Column Card
                const dayCol = document.createElement('div');
                dayCol.className = `group rounded-2xl bg-[#111827] border ${
                    isToday ? 'border-sky-500/70 ring-1 ring-sky-500/30' : 'border-slate-800/90 hover:border-slate-700'
                } flex flex-col justify-between p-3.5 transition cursor-pointer min-h-[280px]`;
                dayCol.onclick = () => openAddRunModal(dayIso);

                // Day Header
                const headerHtml = `
                    <div>
                        <div class="flex items-center justify-between pb-2.5 mb-2.5 border-b border-slate-800/80">
                            <div class="flex items-center gap-2">
                                <span class="text-xs font-bold tracking-wider ${
                                    isToday ? 'text-sky-400' : 'text-slate-400'
                                }">${DAY_NAMES[i]}</span>
                                <span class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${
                                    isToday ? 'bg-sky-500 text-slate-950' : 'text-slate-200 bg-slate-800/80'
                                }">${dayDate.getDate()}</span>
                            </div>
                            <div class="flex items-center gap-1.5">
                                <span class="text-xs font-display font-bold ${
                                    dayMileage > 0 ? 'text-sky-400' : 'text-slate-500'
                                }">${dayMileage.toFixed(1)} mi</span>
                                <button
                                    type="button"
                                    onclick="event.stopPropagation(); openAddRunModal('${dayIso}')"
                                    class="w-6 h-6 rounded-lg bg-slate-800/80 hover:bg-sky-500 hover:text-slate-950 text-slate-400 flex items-center justify-center transition"
                                    title="Add Run on ${dayIso}"
                                >
                                    <span class="material-symbols-outlined text-[15px]">add</span>
                                </button>
                            </div>
                        </div>
                        <div class="day-runs-scroll space-y-2.5 max-h-[380px] overflow-y-auto pr-0.5">
                            ${
                                dayRuns.length === 0
                                    ? `<div class="h-40 rounded-xl border border-dashed border-slate-800/80 group-hover:border-slate-700 flex flex-col items-center justify-center text-slate-500 group-hover:text-slate-400 transition p-3 text-center">
                                            <span class="material-symbols-outlined text-xl mb-1 opacity-60">add_circle</span>
                                            <span class="text-xs font-medium">Click to log run</span>
                                       </div>`
                                    : dayRuns.map(run => renderRunCardHtml(run)).join('')
                            }
                        </div>
                    </div>
                `;

                dayCol.innerHTML = headerHtml;
                grid.appendChild(dayCol);
            }

            // Update Weekly Summary Cards
            document.getElementById('weeklyMileageTotal').textContent = weeklyMileage.toFixed(1);
            const goal = Math.max(1, parseFloat(document.getElementById('weeklyGoalInput').value) || 65);
            const pct = Math.min(100, Math.round((weeklyMileage / goal) * 100));
            document.getElementById('weeklyGoalProgress').style.width = `${pct}%`;
            document.getElementById('weeklyGoalSubtext').textContent = `${pct}% of ${goal} mi weekly target`;

            const totalSessions = runsCache.length;
            document.getElementById('weeklySessionsCount').textContent = String(totalSessions);
            document.getElementById('weeklyDoublesSubtext').textContent = `${amCount} AM / ${pmCount} PM sessions`;

            if (mileageWithTime > 0) {
                const avgPaceSecs = totalSecondsAllRuns / mileageWithTime;
                document.getElementById('weeklyAvgPace').textContent = formatSeconds(avgPaceSecs, false);
            } else {
                document.getElementById('weeklyAvgPace').textContent = '--:--';
            }

            const totalHrs = Math.floor(totalSecondsAllRuns / 3600);
            const totalMins = Math.round((totalSecondsAllRuns % 3600) / 60);
            document.getElementById('weeklyTotalTimeSubtext').textContent = `Total time: ${totalHrs}h ${String(totalMins).padStart(2, '0')}m`;

            document.getElementById('weeklyLongestRun').textContent = longestRun.toFixed(1);
        }

        function renderRunCardHtml(run) {
            const style = getWorkoutStyle(run.workoutType);
            const mileageStr = Number(run.mileage || 0).toFixed(2).replace(/\.00$/, '.0');
            const slot = (run.sessionSlot || 'AM').toUpperCase();
            const paceStr = run.pace ? `${escapeHtml(run.pace)}/mi` : '--:--/mi';
            const timeStr = run.totalTime ? escapeHtml(run.totalTime) : '--:--';
            const notesHtml = run.notes
                ? `<p class="text-[11px] text-slate-400 line-clamp-2 mt-1.5 pt-1.5 border-t border-slate-800/80">${escapeHtml(run.notes)}</p>`
                : '';

            return `
                <div
                    onclick="event.stopPropagation()"
                    class="rounded-xl bg-slate-900/95 border border-slate-800 border-l-4 ${style.border} p-2.5 shadow-sm hover:border-slate-700 transition"
                >
                    <div class="flex items-center justify-between gap-1">
                        <div class="flex items-center gap-1.5">
                            <span class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-slate-800 text-slate-200">${escapeHtml(slot)}</span>
                            <span class="px-2 py-0.5 rounded-full text-[10px] font-semibold border ${style.badge}">
                                ${escapeHtml(run.workoutType || 'Easy')}
                            </span>
                        </div>
                        <button
                            type="button"
                            onclick="deleteRun(event, ${run.id})"
                            class="text-slate-500 hover:text-rose-400 transition p-0.5"
                            title="Delete Run"
                        >
                            <span class="material-symbols-outlined text-[15px]">delete</span>
                        </button>
                    </div>

                    <div class="mt-2 flex items-baseline justify-between">
                        <span class="font-display font-bold text-base text-white">${mileageStr} <span class="text-xs font-normal text-slate-400">mi</span></span>
                        <span class="text-xs font-semibold text-sky-400">${paceStr}</span>
                    </div>

                    <div class="mt-1 flex items-center justify-between text-[11px] text-slate-400">
                        <span class="truncate max-w-[100px]">${escapeHtml(run.athleteName || 'Athlete')}</span>
                        <span>${timeStr}</span>
                    </div>
                    ${notesHtml}
                </div>
            `;
        }

        function escapeHtml(str) {
            return String(str)
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#039;');
        }

        // --- Modal Controls ---
        function openAddRunModal(dateIsoStr) {
            const modal = document.getElementById('addRunModal');
            const form = document.getElementById('addRunForm');
            form.reset();
            recentCalcEdits = [];

            const targetDate = dateIsoStr || toIsoDate(new Date());
            document.getElementById('runDate').value = targetDate;

            const defaultAthlete = document.getElementById('defaultAthleteInput').value.trim() || 'Alex Rivera';
            document.getElementById('athleteName').value = defaultAthlete;

            // Default sessionSlot to PM if an AM run already exists on that date
            const existingOnDate = runsCache.filter(r => r.runDate === targetDate);
            const hasAm = existingOnDate.some(r => (r.sessionSlot || 'AM').toUpperCase() === 'AM');
            document.getElementById('sessionSlot').value = hasAm ? 'PM' : 'AM';

            const statusEl = document.getElementById('calcStatusText');
            statusEl.textContent = 'Tip: Fill in any two fields (e.g., Mileage + Total Time, or Mileage + Pace) and the third updates automatically.';
            statusEl.className = 'text-[11px] text-slate-400';

            modal.classList.remove('hidden');
            modal.classList.add('flex');
            setTimeout(() => document.getElementById('mileage').focus(), 50);
        }

        function closeAddRunModal() {
            const modal = document.getElementById('addRunModal');
            modal.classList.add('hidden');
            modal.classList.remove('flex');
        }

        // Close modal on backdrop click or Escape key
        document.getElementById('addRunModal').addEventListener('click', closeAddRunModal);
        document.addEventListener('keydown', e => {
            if (e.key === 'Escape') closeAddRunModal();
        });

        // --- Week Navigation ---
        function shiftWeek(deltaWeeks) {
            currentWeekMonday = addDays(currentWeekMonday, deltaWeeks * 7);
            fetchRunsForCurrentWeek();
        }

        function goToCurrentWeek() {
            currentWeekMonday = getMonday(new Date());
            fetchRunsForCurrentWeek();
        }

        // Initialize on page load
        document.addEventListener('DOMContentLoaded', () => {
            fetchRunsForCurrentWeek();
        });
    </script>
</body>
</html>

```

---

## Step 4: Verification Commands

Run the following command from the project root to verify compilation, unit tests, and server startup:

```bash
./gradlew bootRun

```

1. Open `http://localhost:8080` in your browser.
2. Click any day card (or `+ Add Run`) to open the modal pre-filled with that date.
3. Enter any two of **Mileage** (e.g. `8`), **Pace** (e.g. `6:30`), or **Total Time** (e.g. `52:00`) and verify the third field auto-calculates immediately.
4. Submit the form to verify it `POST`s to `/api/runs`, renders the run card on that day, updates the **Weekly Mileage Total**, and persists across `./gradlew bootRun` restarts inside `./data/tracklog`.