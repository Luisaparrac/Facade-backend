package com.clinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AppointmentStatus status;

    @Column(name = "reason", length = 200)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    protected Appointment() {
    }

    public Appointment(Long patientId, Doctor doctor, LocalDateTime dateTime, String reason,
                       AppointmentStatus status, LocalDateTime createdAt) {
        this.patientId = patientId;
        this.doctor = doctor;
        this.dateTime = dateTime;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
    }

    public void cancel(LocalDateTime when) {
        this.status = AppointmentStatus.CANCELLED;
        this.cancelledAt = when;
    }

    public boolean isUpcoming(LocalDateTime now) {
        return status == AppointmentStatus.SCHEDULED && dateTime.isAfter(now);
    }

    public boolean isPast(LocalDateTime now) {
        return status != AppointmentStatus.CANCELLED && !dateTime.isAfter(now);
    }

    public Long getId() { return id; }
    public Long getPatientId() { return patientId; }
    public Doctor getDoctor() { return doctor; }
    public LocalDateTime getDateTime() { return dateTime; }
    public AppointmentStatus getStatus() { return status; }
    public String getReason() { return reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
}
