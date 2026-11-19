package com.traininglogbook.training_logbook;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

// Unit tests for POST /api/calculator/solve responses and error handling
@ExtendWith(MockitoExtension.class)
class PaceCalculatorControllerTest {

    @Mock
    private TrainingLogService trainingLogService;

    @InjectMocks
    private PaceCalculatorController paceCalculatorController;

    @Test
    @DisplayName("POST /api/calculator/solve should return HTTP 200 with completed MetricCalculation")
    void testSolveMetricSuccess() {
        MetricSolveRequest request = new MetricSolveRequest(8.0, "6:30", null);
        MetricCalculation expectedResult = new MetricCalculation(8.0, "6:30", "52:00", "totalTime");

        when(trainingLogService.solveMissingMetric(8.0, "6:30", null))
                .thenReturn(expectedResult);

        ResponseEntity<?> response = paceCalculatorController.solveMetric(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
    }

    @Test
    @DisplayName("POST /api/calculator/solve should return HTTP 400 when service throws IllegalArgumentException")
    void testSolveMetricBadRequest() {
        MetricSolveRequest request = new MetricSolveRequest(8.0, null, null);

        when(trainingLogService.solveMissingMetric(8.0, null, null))
                .thenThrow(new IllegalArgumentException("Please provide at least two metrics to calculate the third."));

        ResponseEntity<?> response = paceCalculatorController.solveMetric(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("POST /api/calculator/solve should return HTTP 400 when request body is null")
    void testSolveMetricNullRequest() {
        ResponseEntity<?> response = paceCalculatorController.solveMetric(null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
