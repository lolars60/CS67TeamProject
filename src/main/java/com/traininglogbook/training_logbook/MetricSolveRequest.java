package com.traininglogbook.training_logbook;

// Holds the mileage, pace, and totalTime inputs sent to /api/calculator/solve
public record MetricSolveRequest(
        Double mileage,
        String pace,
        String totalTime
) {}
