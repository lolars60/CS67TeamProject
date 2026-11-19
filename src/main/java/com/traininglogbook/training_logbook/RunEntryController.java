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

// REST endpoints for reading, adding, and deleting runs at /api/runs
@RestController
@RequestMapping("/api/runs")
public class RunEntryController {

    private final RunEntryRepository runEntryRepository;
    private final TrainingLogService trainingLogService;

    // Injects the database repository and calculation service so the controller can read/write to the database
    public RunEntryController(RunEntryRepository runEntryRepository, TrainingLogService trainingLogService) {
        this.runEntryRepository = runEntryRepository;
        this.trainingLogService = trainingLogService;
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

    // GET /api/runs/summary - returns weekly mileage, average pace, and daily totals from Java
    @GetMapping("/summary")
    public WeeklySummary getWeeklySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "65") double goal) {
        return trainingLogService.calculateWeeklySummary(startDate, endDate, goal);
    }

    // POST /api/runs - fills defaults and missing metrics, then saves the run to H2
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

        // Fill in any missing pace, mileage, or time value before saving
        trainingLogService.autoFillRunMetrics(runEntry);

        RunEntry saved = runEntryRepository.save(runEntry);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // DELETE /api/runs/{id} - deletes a run by its ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRun(@PathVariable Long id) {
        if (!runEntryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        runEntryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
