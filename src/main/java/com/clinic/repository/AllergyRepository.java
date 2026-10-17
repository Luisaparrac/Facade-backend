package com.clinic.repository;

import com.clinic.model.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllergyRepository extends JpaRepository<Allergy, Long> {

    List<Allergy> findByPatientIdOrderBySubstanceAsc(Long patientId);

    boolean existsByPatientIdAndSubstanceIgnoreCase(Long patientId, String substance);
}
