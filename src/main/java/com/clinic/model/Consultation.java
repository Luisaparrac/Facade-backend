package com.clinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultations")
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "doctor_name", nullable = false, length = 100)
    private String doctorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "specialty", nullable = false, length = 30)
    private Specialty specialty;

    @Column(name = "consultation_date", nullable = false)
    private LocalDateTime consultationDate;

    @Column(name = "reason", nullable = false, length = 200)
    private String reason;

    @Column(name = "diagnosis", nullable = false, length = 200)
    private String diagnosis;

    @Column(name = "notes", length = 500)
    private String notes;

    protected Consultation() {
    }

    public Consultation(Long patientId, String doctorName, Specialty specialty, LocalDateTime consultationDate,
                        String reason, String diagnosis, String notes) {
        this.patientId = patientId;
        this.doctorName = doctorName;
        this.specialty = specialty;
        this.consultationDate = consultationDate;
        this.reason = reason;
        this.diagnosis = diagnosis;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public Long getPatientId() { return patientId; }
    public String getDoctorName() { return doctorName; }
    public Specialty getSpecialty() { return specialty; }
    public LocalDateTime getConsultationDate() { return consultationDate; }
    public String getReason() { return reason; }
    public String getDiagnosis() { return diagnosis; }
    public String getNotes() { return notes; }
}
