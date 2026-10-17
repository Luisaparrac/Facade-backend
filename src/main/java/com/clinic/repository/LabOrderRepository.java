package com.clinic.repository;

import com.clinic.model.LabOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {

    List<LabOrder> findByPatientIdOrderByRequestedAtDesc(Long patientId);
}
