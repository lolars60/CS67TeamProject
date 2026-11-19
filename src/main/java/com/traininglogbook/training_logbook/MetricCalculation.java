package com.traininglogbook.training_logbook;

// Holds the 3 solved run metrics plus which field was calculated
public record MetricCalculation(
        Double mileage,
        String pace,
        String totalTime,
        String calculatedField
) {}
