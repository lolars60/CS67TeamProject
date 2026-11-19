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

// Handles HTTP requests from the frontend at /api/runs and returns JSON data
@RestController
@RequestMapping("/api/runs")
public class RunEntryController {

    private final RunEntryRepository runEntryRepository;

    // Injects the repository so the controller can read/write to the database
    public RunEntryController(RunEntryRepository runEntryRepository) {
        this.runEntryRepository = runEntryRepository;
    }

    // GET /api/runs - Returns runs for a specific week (if startDate and endDate are give) or all runs if no dates are given
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

    // Post /api/runs - Takes JSON from the Add Run modal, sets default values if missing, and saves to the database
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

    // DELETE /api/run/{id} - Deletes a specific run by its database ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRun(@PathVariable Long id) {
        if (!runEntryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        runEntryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
