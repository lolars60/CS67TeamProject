package com.traininglogbook.training_logbook;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * CS67 Team Project - Team Logbook (Carter Beasley & Lanse Larsen)
 *
 * Milestone 1: 3-Way Running Calculator Backend (Spring Boot WebMVC)
 *
 * How this class works with the Spring Boot framework:
 * 1. @RestController tells Spring Boot to register this class as a web controller
 *    where every method returns data (automatically converted to JSON by Jackson)
 *    directly in the HTTP response body.
 * 2. Because this class is in the "com.traininglogbook.training_logbook" package,
 *    @SpringBootApplication in TrainingLogbookApplication.java automatically finds
 *    and loads it when the server starts on http://localhost:8080.
 */
@RestController
public class PaceCalculatorController {

    /**
     * Java 25 Record representing the JSON response sent back to the web page.
     * Spring Boot's built-in Jackson library automatically serializes this record
     * into JSON format, for example:
     * {
     *   "miles": 8.0,
     *   "pace": "6:30",
     *   "time": "52:00",
     *   "calculatedField": "time"
     * }
     */
    public record CalculationResponse(
            double miles,
            String pace,
            String time,
            String calculatedField
    ) {}

    /**
     * Handles HTTP GET requests sent to: http://localhost:8080/api/calculate
     *
     * Example URLs:
     * - Calculate Time:    /api/calculate?miles=8&pace=6:30
     * - Calculate Pace:    /api/calculate?miles=8&time=52:00
     * - Calculate Mileage: /api/calculate?pace=6:30&time=52:00
     *
     * @param miles Optional mileage input from the query string (?miles=...).
     * @param pace  Optional pace input in "mm:ss" format (?pace=...).
     * @param time  Optional total time input in "mm:ss" or "hh:mm:ss" format (?time=...).
     * @return ResponseEntity containing either a CalculationResponse (HTTP 200 OK)
     *         or an error message map (HTTP 400 Bad Request).
     */
    @GetMapping("/api/calculate")
    public ResponseEntity<?> calculate(
            @RequestParam(required = false) Double miles,
            @RequestParam(required = false) String pace,
            @RequestParam(required = false) String time
    ) {
        // Step 1: Check which of the 3 inputs were actually provided by the user.
        boolean hasMiles = miles != null && miles > 0;
        boolean hasPace = pace != null && !pace.isBlank();
        boolean hasTime = time != null && !time.isBlank();

        // Count how many valid inputs were given. We need exactly 2 inputs to solve for the 3rd.
        int providedCount = (hasMiles ? 1 : 0) + (hasPace ? 1 : 0) + (hasTime ? 1 : 0);
        if (providedCount != 2) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Please enter exactly two values and leave the one you want to calculate blank.")
            );
        }

        try {
            // Case 1: User entered Mileage and Pace -> Solve for Total Time
            // Formula: Total Time (seconds) = Mileage * Pace (seconds per mile)
            if (!hasTime) {
                int paceSecondsPerMile = parseDurationToSeconds(pace);
                if (paceSecondsPerMile <= 0) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Pace must be greater than 0:00."));
                }
                int calculatedTotalSeconds = (int) Math.round(miles * paceSecondsPerMile);

                return ResponseEntity.ok(new CalculationResponse(
                        roundTwoDecimals(miles),
                        formatSeconds(paceSecondsPerMile, false),
                        formatSeconds(calculatedTotalSeconds, true),
                        "time"
                ));
            }

            // Case 2: User entered Mileage and Total Time -> Solve for Pace
            // Formula: Pace (seconds per mile) = Total Time (seconds) / Mileage
            if (!hasPace) {
                int totalSeconds = parseDurationToSeconds(time);
                if (totalSeconds <= 0) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Total time must be greater than 0:00."));
                }
                int calculatedPaceSeconds = (int) Math.round(totalSeconds / miles);

                return ResponseEntity.ok(new CalculationResponse(
                        roundTwoDecimals(miles),
                        formatSeconds(calculatedPaceSeconds, false),
                        formatSeconds(totalSeconds, true),
                        "pace"
                ));
            }

            // Case 3: User entered Pace and Total Time -> Solve for Mileage
            // Formula: Mileage = Total Time (seconds) / Pace (seconds per mile)
            int totalSeconds = parseDurationToSeconds(time);
            int paceSecondsPerMile = parseDurationToSeconds(pace);
            if (paceSecondsPerMile <= 0 || totalSeconds <= 0) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Both pace and total time must be greater than 0:00.")
                );
            }
            double calculatedMiles = roundTwoDecimals((double) totalSeconds / paceSecondsPerMile);

            return ResponseEntity.ok(new CalculationResponse(
                    calculatedMiles,
                    formatSeconds(paceSecondsPerMile, false),
                    formatSeconds(totalSeconds, true),
                    "miles"
            ));

        } catch (IllegalArgumentException e) {
            // If the user typed an invalid time format (e.g., "6:75" or letters), return a 400 error message.
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Helper method that converts a time string ("mm:ss" or "hh:mm:ss") into total seconds.
     * Converting to integer seconds avoids floating-point rounding bugs with minutes.
     */
    private int parseDurationToSeconds(String input) {
        String[] parts = input.trim().split(":");
        try {
            if (parts.length == 2) {
                // Format: mm:ss (e.g., "6:30" or "52:00")
                int minutes = Integer.parseInt(parts[0]);
                int seconds = Integer.parseInt(parts[1]);
                if (minutes < 0 || seconds < 0 || seconds >= 60) {
                    throw new IllegalArgumentException("Seconds must be between 00 and 59 (use mm:ss).");
                }
                return (minutes * 60) + seconds;
            } else if (parts.length == 3) {
                // Format: hh:mm:ss (e.g., "1:05:30")
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                int seconds = Integer.parseInt(parts[2]);
                if (hours < 0 || minutes < 0 || minutes >= 60 || seconds < 0 || seconds >= 60) {
                    throw new IllegalArgumentException("Minutes and seconds must be between 00 and 59 (use hh:mm:ss).");
                }
                return (hours * 3600) + (minutes * 60) + seconds;
            } else {
                throw new IllegalArgumentException("Use mm:ss for pace and mm:ss or hh:mm:ss for total time.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Time and pace must be numbers separated by ':' (e.g., 6:30).");
        }
    }

    /**
     * Helper method that formats total seconds back into a readable "mm:ss" or "hh:mm:ss" string.
     */
    private String formatSeconds(int totalSeconds, boolean useHoursWhenOverSixtyMinutes) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (useHoursWhenOverSixtyMinutes && hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        int totalMinutes = totalSeconds / 60;
        return String.format("%d:%02d", totalMinutes, seconds);
    }

    /**
     * Rounds mileage to 2 decimal places (e.g., 8.125 -> 8.13).
     */
    private double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
