package com.clinic.repository;

import com.clinic.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Patient> findByDocumentNumber(String documentNumber);
}
