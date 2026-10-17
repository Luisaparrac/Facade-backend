package com.clinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "prescription_items")
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medication", nullable = false, length = 100)
    private String medication;

    @Column(name = "dose", nullable = false, length = 60)
    private String dose;

    @Column(name = "frequency", nullable = false, length = 60)
    private String frequency;

    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @Column(name = "instructions", length = 200)
    private String instructions;

    protected PrescriptionItem() {
    }

    public PrescriptionItem(String medication, String dose, String frequency, int durationDays, String instructions) {
        this.medication = medication;
        this.dose = dose;
        this.frequency = frequency;
        this.durationDays = durationDays;
        this.instructions = instructions;
    }

    public Long getId() { return id; }
    public String getMedication() { return medication; }
    public String getDose() { return dose; }
    public String getFrequency() { return frequency; }
    public int getDurationDays() { return durationDays; }
    public String getInstructions() { return instructions; }
}
