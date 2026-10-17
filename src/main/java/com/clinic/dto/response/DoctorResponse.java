package com.clinic.dto.response;

import com.clinic.model.Doctor;
import com.clinic.model.Specialty;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record DoctorResponse(
        Long id,
        String fullName,
        Specialty specialty,
        String specialtyName,
        String licenseNumber,
        LocalDate date,
        List<String> availableSlots) {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    public static DoctorResponse from(Doctor doctor, LocalDate date, List<LocalTime> availableSlots) {
        return new DoctorResponse(doctor.getId(), doctor.getFullName(), doctor.getSpecialty(),
                doctor.getSpecialty().getDisplayName(), doctor.getLicenseNumber(), date,
                availableSlots.stream().map(TIME::format).toList());
    }
}
