package com.traininglogbook.training_logbook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Unit tests for pace/time/mileage calculations and weekly summary totals
@ExtendWith(MockitoExtension.class)
class TrainingLogServiceTest {

    @Mock
    private RunEntryRepository runEntryRepository;

    @InjectMocks
    private TrainingLogService trainingLogService;

    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2026, 8, 24);
        endDate = LocalDate.of(2026, 8, 30);
    }

    @Test
    @DisplayName("Should correctly calculate missing totalTime from mileage and pace")
    void testSolveMissingTotalTime() {
        // 8.0 miles at 6:30 pace -> 52:00
        MetricCalculation result = trainingLogService.solveMissingMetric(8.0, "6:30", null);

        assertNotNull(result);
        assertEquals(8.0, result.mileage());
        assertEquals("6:30", result.pace());
        assertEquals("52:00", result.totalTime());
        assertEquals("totalTime", result.calculatedField());
    }

    @Test
    @DisplayName("Should correctly calculate missing pace from mileage and totalTime")
    void testSolveMissingPace() {
        // 8.0 miles in 52:00 -> 6:30 pace
        MetricCalculation result = trainingLogService.solveMissingMetric(8.0, null, "52:00");

        assertNotNull(result);
        assertEquals(8.0, result.mileage());
        assertEquals("6:30", result.pace());
        assertEquals("52:00", result.totalTime());
        assertEquals("pace", result.calculatedField());
    }

    @Test
    @DisplayName("Should correctly calculate missing mileage from pace and totalTime")
    void testSolveMissingMileage() {
        // 6:30 pace for 52:00 -> 8.0 miles
        MetricCalculation result = trainingLogService.solveMissingMetric(null, "6:30", "52:00");

        assertNotNull(result);
        assertEquals(8.0, result.mileage());
        assertEquals("6:30", result.pace());
        assertEquals("52:00", result.totalTime());
        assertEquals("mileage", result.calculatedField());
    }

    @Test
    @DisplayName("Should auto-fill missing metrics in RunEntry before database persistence")
    void testAutoFillRunMetrics() {
        RunEntry entry = new RunEntry(
                "Carter",
                LocalDate.of(2026, 8, 24),
                "AM",
                5.0,
                "7:00",
                null,
                "Easy",
                "Recovery jog"
        );

        trainingLogService.autoFillRunMetrics(entry);

        // 5.0 miles at 7:00 pace = 35:00 total time
        assertEquals("35:00", entry.getTotalTime());
        assertEquals("7:00", entry.getPace());
        assertEquals(5.0, entry.getMileage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when fewer than two valid metrics are provided")
    void testSolveMissingMetricInsufficientInputs() {
        assertThrows(IllegalArgumentException.class, () ->
                trainingLogService.solveMissingMetric(8.0, null, null)
        );
    }

    @Test
    @DisplayName("Should compute accurate WeeklySummary across multiple runs including AM/PM double sessions")
    void testCalculateWeeklySummaryWithDoubles() {
        // Monday AM/PM double, Wednesday tempo, Saturday long run
        RunEntry monAm = new RunEntry("Carter", LocalDate.of(2026, 8, 24), "AM", 6.0, "7:00", "42:00", "Easy", "Morning loop");
        RunEntry monPm = new RunEntry("Carter", LocalDate.of(2026, 8, 24), "PM", 4.0, "7:30", "30:00", "Shakeout", "Evening double");
        RunEntry wed = new RunEntry("Carter", LocalDate.of(2026, 8, 26), "Run 1", 10.0, "6:30", "1:05:00", "Tempo", "Midweek workout");
        RunEntry sat = new RunEntry("Carter", LocalDate.of(2026, 8, 29), "Run 1", 12.0, "6:40", "1:20:00", "Long Run", "Weekly long run");

        when(runEntryRepository.findByRunDateBetween(startDate, endDate))
                .thenReturn(List.of(monAm, monPm, wed, sat));

        WeeklySummary summary = trainingLogService.calculateWeeklySummary(startDate, endDate, 40.0);

        assertNotNull(summary);
        // Total mileage: 6.0 + 4.0 + 10.0 + 12.0 = 32.0
        assertEquals(32.0, summary.getTotalMileage());
        assertEquals(4, summary.getSessionCount());
        assertEquals(3, summary.getAmCount());
        assertEquals(1, summary.getPmCount());
        assertEquals(12.0, summary.getLongestRun());

        // Total seconds: 2520 + 1800 + 3900 + 4800 = 13020s
        // Weighted average pace: 13020 / 32.0 = 406.875s -> 407s -> 6:47
        assertEquals("6:47", summary.getAveragePace());

        // Total time formatted: 13020s = 3h 37m
        assertEquals("3h 37m", summary.getTotalTimeFormatted());

        // Goal percentage: (32.0 / 40.0) * 100 = 80%
        assertEquals(80, summary.getGoalPercentage());

        // Daily totals verification
        Map<String, Double> dailyTotals = summary.getDailyTotals();
        assertNotNull(dailyTotals);
        assertEquals(7, dailyTotals.size());
        assertEquals(10.0, dailyTotals.get("2026-08-24")); // 6.0 + 4.0 double
        assertEquals(0.0, dailyTotals.get("2026-08-25"));
        assertEquals(10.0, dailyTotals.get("2026-08-26"));
        assertEquals(0.0, dailyTotals.get("2026-08-27"));
        assertEquals(0.0, dailyTotals.get("2026-08-28"));
        assertEquals(12.0, dailyTotals.get("2026-08-29"));
        assertEquals(0.0, dailyTotals.get("2026-08-30"));
    }
}
