package com.clinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "specialty", nullable = false, length = 30)
    private Specialty specialty;

    @Column(name = "license_number", nullable = false, unique = true, length = 20)
    private String licenseNumber;

    protected Doctor() {
    }

    public Doctor(String fullName, Specialty specialty, String licenseNumber) {
        this.fullName = fullName;
        this.specialty = specialty;
        this.licenseNumber = licenseNumber;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public Specialty getSpecialty() { return specialty; }
    public String getLicenseNumber() { return licenseNumber; }
}
