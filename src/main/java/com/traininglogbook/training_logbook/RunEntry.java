package com.traininglogbook.training_logbook;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Marks this class as a SQL database table named "run_entries"
@Entity
@Table(name = "run_entries")
public class RunEntry {

    // Primary key that automatically increments for each new entry
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String athleteName;
    private LocalDate runDate;    // Stored as YYY-MM-DD
    private String sessionSlot;   // "AM" or "PM" for double runs
    private Double mileage;       // Distance in miles
    private String pace;          // Stored as "MM:SS"
    private String totalTime;     // Stored as "MM:SS" or "HH:MM:SS"
    private String workoutType;   // Easy, Wprkout, Long Run, Tempo, Recovery, etc.

    // Allow longer text (up to 1000 characters) for notes and workout details
    @Column(length = 1000)
    private String notes;

    // Default constructor required by JPA
    public RunEntry() {
    }

    // Constructor used when creating a new RunEntry object
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

    // Getters and setters used by Spring to convert between Java objects and json
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
