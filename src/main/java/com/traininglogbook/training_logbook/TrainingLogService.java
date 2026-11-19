package com.traininglogbook.training_logbook;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

// Handles weekly stats math and the 3-way pace/mileage/time calcuations
@Service
public class TrainingLogService {

    private final RunEntryRepository runEntryRepository;

    // Injects the repository so this service can pull runs from H2
    public TrainingLogService(RunEntryRepository runEntryRepository) {
        this.runEntryRepository = runEntryRepository;
    }

    // Sums weekly mileage, AM/PM counts, longest run, weighted average pace, and daily chart totals
    public WeeklySummary calculateWeeklySummary(LocalDate startDate, LocalDate endDate, double weeklyGoal) {
        List<RunEntry> runs = runEntryRepository.findByRunDateBetween(startDate, endDate);

        double totalMileage = 0.0;
        int sessionCount = runs.size();
        int amCount = 0;
        int pmCount = 0;
        double longestRun = 0.0;

        int totalTimedSeconds = 0;
        double mileageWithTime = 0.0;

        // Initialize dailyTotals for every day in the range with 0.0
        Map<String, Double> dailyTotals = new LinkedHashMap<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dailyTotals.put(current.toString(), 0.0);
            current = current.plusDays(1);
        }

        for (RunEntry run : runs) {
            double miles = run.getMileage() != null ? run.getMileage() : 0.0;
            totalMileage += miles;

            if (miles > longestRun) {
                longestRun = miles;
            }

            // Categorize session slots (AM vs PM)
            if (isAmSession(run.getSessionSlot())) {
                amCount++;
            } else if (isPmSession(run.getSessionSlot())) {
                pmCount++;
            }

            // Map mileage to corresponding day
            if (run.getRunDate() != null) {
                String dateKey = run.getRunDate().toString();
                if (dailyTotals.containsKey(dateKey)) {
                    dailyTotals.put(dateKey, roundTwoDecimals(dailyTotals.get(dateKey) + miles));
                }
            }

            // Resolve duration in seconds (from totalTime or from mileage * pace)
            int runSeconds = 0;
            if (run.getTotalTime() != null && !run.getTotalTime().isBlank() && !run.getTotalTime().equals("—")) {
                try {
                    runSeconds = parseDurationToSeconds(run.getTotalTime());
                } catch (IllegalArgumentException ignored) {
                    runSeconds = 0;
                }
            } else if (miles > 0 && run.getPace() != null && !run.getPace().isBlank() && !run.getPace().equals("—")) {
                try {
                    int paceSec = parseDurationToSeconds(run.getPace());
                    runSeconds = (int) Math.round(miles * paceSec);
                } catch (IllegalArgumentException ignored) {
                    runSeconds = 0;
                }
            }

            if (runSeconds > 0 && miles > 0) {
                totalTimedSeconds += runSeconds;
                mileageWithTime += miles;
            }
        }

        // Weighted average pace: total timed seconds / total timed miles
        String averagePace;
        if (mileageWithTime > 0 && totalTimedSeconds > 0) {
            int avgPaceSec = (int) Math.round((double) totalTimedSeconds / mileageWithTime);
            averagePace = formatSeconds(avgPaceSec, false);
        } else {
            averagePace = "--:--";
        }

        // Formatted total duration (e.g., "4h 15m" or "45m")
        int hours = totalTimedSeconds / 3600;
        int minutes = (totalTimedSeconds % 3600) / 60;
        String totalTimeFormatted;
        if (hours > 0) {
            totalTimeFormatted = String.format("%dh %dm", hours, minutes);
        } else {
            totalTimeFormatted = String.format("%dm", minutes);
        }

        // Goal percentage capped at 0-100
        int goalPercentage = 0;
        if (weeklyGoal > 0) {
            goalPercentage = (int) Math.round((totalMileage / weeklyGoal) * 100.0);
            goalPercentage = Math.min(100, Math.max(0, goalPercentage));
        }

        return new WeeklySummary(
                roundTwoDecimals(totalMileage),
                sessionCount,
                amCount,
                pmCount,
                roundTwoDecimals(longestRun),
                averagePace,
                totalTimeFormatted,
                goalPercentage,
                dailyTotals
        );
    }

    // Calculates the missing 3rd value (mileage, pace, or totalTime)
    public MetricCalculation solveMissingMetric(Double mileage, String pace, String totalTime) {
        boolean hasMiles = mileage != null && mileage > 0;
        boolean hasPace = pace != null && !pace.isBlank() && !pace.equals("—") && !pace.equals("-");
        boolean hasTime = totalTime != null && !totalTime.isBlank() && !totalTime.equals("—") && !totalTime.equals("-");

        int count = (hasMiles ? 1 : 0) + (hasPace ? 1 : 0) + (hasTime ? 1 : 0);
        if (count < 2) {
            throw new IllegalArgumentException("Please provide at least two metrics to calculate the third.");
        }

        // Case 1: Solve for Total Time (Mileage * Pace (sec/mi))
        if (!hasTime) {
            int paceSec = parseDurationToSeconds(pace);
            if (paceSec <= 0) {
                throw new IllegalArgumentException("Pace must be greater than 0:00.");
            }
            int calcTimeSec = (int) Math.round(mileage * paceSec);
            return new MetricCalculation(
                    roundTwoDecimals(mileage),
                    formatSeconds(paceSec, false),
                    formatSeconds(calcTimeSec, true),
                    "totalTime"
            );
        }

        // Case 2: Solve for Pace (Total Time (sec) / Mileage)
        if (!hasPace) {
            int timeSec = parseDurationToSeconds(totalTime);
            if (timeSec <= 0) {
                throw new IllegalArgumentException("Total time must be greater than 0:00.");
            }
            int calcPaceSec = (int) Math.round((double) timeSec / mileage);
            return new MetricCalculation(
                    roundTwoDecimals(mileage),
                    formatSeconds(calcPaceSec, false),
                    formatSeconds(timeSec, true),
                    "pace"
            );
        }

        // Case 3: Solve for Mileage (Total Time (sec) / Pace (sec/mi))
        if (!hasMiles) {
            int paceSec = parseDurationToSeconds(pace);
            int timeSec = parseDurationToSeconds(totalTime);
            if (paceSec <= 0 || timeSec <= 0) {
                throw new IllegalArgumentException("Both pace and total time must be greater than 0:00.");
            }
            double calcMiles = roundTwoDecimals((double) timeSec / paceSec);
            return new MetricCalculation(
                    calcMiles,
                    formatSeconds(paceSec, false),
                    formatSeconds(timeSec, true),
                    "mileage"
            );
        }

        // All three provided: format and return without altering
        int paceSec = parseDurationToSeconds(pace);
        int timeSec = parseDurationToSeconds(totalTime);
        return new MetricCalculation(
                roundTwoDecimals(mileage),
                formatSeconds(paceSec, false),
                formatSeconds(timeSec, true),
                "none"
        );
    }

    // Fills in a missing metric on a RunEntry before it gets saved to the database
    public void autoFillRunMetrics(RunEntry runEntry) {
        if (runEntry == null) return;

        boolean hasMiles = runEntry.getMileage() != null && runEntry.getMileage() > 0;
        boolean hasPace = runEntry.getPace() != null && !runEntry.getPace().isBlank() && !runEntry.getPace().equals("—");
        boolean hasTime = runEntry.getTotalTime() != null && !runEntry.getTotalTime().isBlank() && !runEntry.getTotalTime().equals("—");

        int count = (hasMiles ? 1 : 0) + (hasPace ? 1 : 0) + (hasTime ? 1 : 0);
        if (count == 2) {
            try {
                MetricCalculation solved = solveMissingMetric(
                        runEntry.getMileage(),
                        runEntry.getPace(),
                        runEntry.getTotalTime()
                );
                runEntry.setMileage(solved.mileage());
                runEntry.setPace(solved.pace());
                runEntry.setTotalTime(solved.totalTime());
            } catch (Exception ignored) {
                // Keep original values if parsing fails
            }
        }
    }

    // Turns "MM:SS" or "HH:MM:SS" into total seconds for calculations
    public int parseDurationToSeconds(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Duration string cannot be blank.");
        }
        String[] parts = input.trim().split(":");
        try {
            if (parts.length == 2) {
                int minutes = Integer.parseInt(parts[0]);
                int seconds = Integer.parseInt(parts[1]);
                if (minutes < 0 || seconds < 0 || seconds >= 60) {
                    throw new IllegalArgumentException("Seconds must be between 00 and 59.");
                }
                return (minutes * 60) + seconds;
            } else if (parts.length == 3) {
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                int seconds = Integer.parseInt(parts[2]);
                if (hours < 0 || minutes < 0 || minutes >= 60 || seconds < 0 || seconds >= 60) {
                    throw new IllegalArgumentException("Minutes and seconds must be between 00 and 59.");
                }
                return (hours * 3600) + (minutes * 60) + seconds;
            } else {
                throw new IllegalArgumentException("Use MM:SS or HH:MM:SS format.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Time and pace must be numbers separated by ':' (e.g., 6:30).");
        }
    }

    // Formats total seconds back into "MM:SS" or "HH:MM:SS"
    public String formatSeconds(int totalSeconds, boolean useHoursWhenOverSixtyMinutes) {
        if (totalSeconds < 0) totalSeconds = 0;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (useHoursWhenOverSixtyMinutes && hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        int totalMinutes = totalSeconds / 60;
        return String.format("%d:%02d", totalMinutes, seconds);
    }

    // Rounds mileage to 2 decimal places for display
    public double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    // Checks whether a sessionSlot counts as Run 1 (AM)
    private boolean isAmSession(String slot) {
        if (slot == null || slot.isBlank()) return true;
        String s = slot.trim().toUpperCase();
        return s.equals("AM") || s.equals("RUN 1") || s.equals("SESSION 1") || s.equals("1") || s.equals("RUN1");
    }

    // Checks whether a sessionSlot counts as Run 2 (PM)
    private boolean isPmSession(String slot) {
        if (slot == null || slot.isBlank()) return false;
        String s = slot.trim().toUpperCase();
        return s.equals("PM") || s.equals("RUN 2") || s.equals("SESSION 2") || s.equals("2") || s.equals("RUN2");
    }
}
