package com.traininglogbook.training_logbook;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "run_entries")
public class RunEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String athleteName;

    private LocalDate runDate;

    // "AM" or "PM" to support daily doubles
    private String sessionSlot;

    private Double mileage;

    // Formatted as "MM:SS" (e.g., "6:45")
    private String pace;

    // Formatted as "MM:SS" or "HH:MM:SS" (e.g., "54:00")
    private String totalTime;

    // e.g., "Easy", "Workout", "Long Run", "Tempo", "Recovery", "Race"
    private String workoutType;

    @Column(length = 1000)
    private String notes;

    public RunEntry() {
    }

    public RunEntry(
            String athleteName,
            LocalDate runDate,
            String sessionSlot,
            Double mileage,
            String pace,
            String totalTime,
            String workoutType,
            String notes) {
        this.athleteName = athleteName;
        this.runDate = runDate;
        this.sessionSlot = sessionSlot;
        this.mileage = mileage;
        this.pace = pace;
        this.totalTime = totalTime;
        this.workoutType = workoutType;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAthleteName() {
        return athleteName;
    }

    public void setAthleteName(String athleteName) {
        this.athleteName = athleteName;
    }

    public LocalDate getRunDate() {
        return runDate;
    }

    public void setRunDate(LocalDate runDate) {
        this.runDate = runDate;
    }

    public String getSessionSlot() {
        return sessionSlot;
    }

    public void setSessionSlot(String sessionSlot) {
        this.sessionSlot = sessionSlot;
    }

    public Double getMileage() {
        return mileage;
    }

    public void setMileage(Double mileage) {
        this.mileage = mileage;
    }

    public String getPace() {
        return pace;
    }

    public void setPace(String pace) {
        this.pace = pace;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public String getWorkoutType() {
        return workoutType;
    }

    public void setWorkoutType(String workoutType) {
        this.workoutType = workoutType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
