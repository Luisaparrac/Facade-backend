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
@Table(name = "lab_orders")
public class LabOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "lab_order_id", nullable = false)
    @OrderBy("id")
    private List<LabResult> results = new ArrayList<>();

    protected LabOrder() {
    }

    public LabOrder(Long patientId, LocalDateTime requestedAt) {
        this.patientId = patientId;
        this.requestedAt = requestedAt;
    }

    public void addResult(LabResult result) {
        results.add(result);
    }

    public void complete(LocalDateTime when) {
        this.completedAt = when;
    }

    public long countOutOfRange() {
        return results.stream().filter(result -> result.getStatus() != ResultStatus.NORMAL).count();
    }

    public Long getId() { return id; }
    public Long getPatientId() { return patientId; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public List<LabResult> getResults() { return Collections.unmodifiableList(results); }
}
