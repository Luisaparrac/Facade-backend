package com.clinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "allergies")
public class Allergy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "substance", nullable = false, length = 80)
    private String substance;

    @Column(name = "reaction", length = 120)
    private String reaction;

    protected Allergy() {
    }

    public Allergy(Long patientId, String substance, String reaction) {
        this.patientId = patientId;
        this.substance = substance;
        this.reaction = reaction;
    }

    public Long getId() { return id; }
    public Long getPatientId() { return patientId; }
    public String getSubstance() { return substance; }
    public String getReaction() { return reaction; }
}
