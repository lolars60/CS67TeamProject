package com.traininglogbook.training_logbook;

import java.util.Map;

// Holds the weekly stats returned by GET /api/runs/summary
public class WeeklySummary {

    private double totalMileage;
    private int sessionCount;
    private int amCount;
    private int pmCount;
    private double longestRun;
    private String averagePace;
    private String totalTimeFormatted;
    private int goalPercentage;
    private Map<String, Double> dailyTotals;

    // Empty constructor needed by Springboot for JSON
    public WeeklySummary() {
    }

    // Builds a WeeklySummary with all calculated stats
    public WeeklySummary(
            double totalMileage,
            int sessionCount,
            int amCount,
            int pmCount,
            double longestRun,
            String averagePace,
            String totalTimeFormatted,
            int goalPercentage,
            Map<String, Double> dailyTotals) {
        this.totalMileage = totalMileage;
        this.sessionCount = sessionCount;
        this.amCount = amCount;
        this.pmCount = pmCount;
        this.longestRun = longestRun;
        this.averagePace = averagePace;
        this.totalTimeFormatted = totalTimeFormatted;
        this.goalPercentage = goalPercentage;
        this.dailyTotals = dailyTotals;
    }

    public double getTotalMileage() {
        return totalMileage;
    }

    public void setTotalMileage(double totalMileage) {
        this.totalMileage = totalMileage;
    }

    public int getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(int sessionCount) {
        this.sessionCount = sessionCount;
    }

    public int getAmCount() {
        return amCount;
    }

    public void setAmCount(int amCount) {
        this.amCount = amCount;
    }

    public int getPmCount() {
        return pmCount;
    }

    public void setPmCount(int pmCount) {
        this.pmCount = pmCount;
    }

    public double getLongestRun() {
        return longestRun;
    }

    public void setLongestRun(double longestRun) {
        this.longestRun = longestRun;
    }

    public String getAveragePace() {
        return averagePace;
    }

    public void setAveragePace(String averagePace) {
        this.averagePace = averagePace;
    }

    public String getTotalTimeFormatted() {
        return totalTimeFormatted;
    }

    public void setTotalTimeFormatted(String totalTimeFormatted) {
        this.totalTimeFormatted = totalTimeFormatted;
    }

    public int getGoalPercentage() {
        return goalPercentage;
    }

    public void setGoalPercentage(int goalPercentage) {
        this.goalPercentage = goalPercentage;
    }

    public Map<String, Double> getDailyTotals() {
        return dailyTotals;
    }

    public void setDailyTotals(Map<String, Double> dailyTotals) {
        this.dailyTotals = dailyTotals;
    }
}
