package com.clinic.repository;

import com.clinic.model.Appointment;
import com.clinic.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsByDoctor_IdAndDateTimeAndStatus(Long doctorId, LocalDateTime dateTime, AppointmentStatus status);

    boolean existsByPatientIdAndDateTimeAndStatus(Long patientId, LocalDateTime dateTime, AppointmentStatus status);

    List<Appointment> findByDoctor_IdAndStatusAndDateTimeBetween(Long doctorId, AppointmentStatus status,
                                                                 LocalDateTime start, LocalDateTime end);

    List<Appointment> findByPatientIdOrderByDateTimeAsc(Long patientId);
}
