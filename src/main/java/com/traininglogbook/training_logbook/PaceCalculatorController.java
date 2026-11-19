package com.traininglogbook.training_logbook;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// Handles calculator requests from the Add Run modal
@RestController
public class PaceCalculatorController {

    private final TrainingLogService trainingLogService;

    // Injects TrainingLogService to run the pace/time/mileage math
    public PaceCalculatorController(TrainingLogService trainingLogService) {
        this.trainingLogService = trainingLogService;
    }

    // POST /api/calculator/solve - takes 2 inputs as JSON and returns all 3 solved values
    @PostMapping("/api/calculator/solve")
    public ResponseEntity<?> solveMetric(@RequestBody MetricSolveRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Request body cannot be empty."));
        }

        try {
            MetricCalculation result = trainingLogService.solveMissingMetric(
                    request.mileage(),
                    request.pace(),
                    request.totalTime()
            );
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
