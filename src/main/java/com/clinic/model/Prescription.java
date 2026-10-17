package com.clinic.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "doctor_name", nullable = false, length = 100)
    private String doctorName;

    @Column(name = "diagnosis", length = 200)
    private String diagnosis;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "prescription_id", nullable = false)
    @OrderBy("id")
    private List<PrescriptionItem> items = new ArrayList<>();

    protected Prescription() {
    }

    public Prescription(Long patientId, String doctorName, String diagnosis, LocalDateTime issuedAt) {
        this.patientId = patientId;
        this.doctorName = doctorName;
        this.diagnosis = diagnosis;
        this.issuedAt = issuedAt;
    }

    public void addItem(PrescriptionItem item) {
        items.add(item);
    }

    public Long getId() { return id; }
    public Long getPatientId() { return patientId; }
    public String getDoctorName() { return doctorName; }
    public String getDiagnosis() { return diagnosis; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public List<PrescriptionItem> getItems() { return Collections.unmodifiableList(items); }
}
