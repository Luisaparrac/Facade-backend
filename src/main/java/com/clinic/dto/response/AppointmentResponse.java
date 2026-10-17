package com.clinic.dto.response;

import com.clinic.model.Appointment;
import com.clinic.model.AppointmentStatus;
import com.clinic.model.Specialty;

import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long patientId,
        Long doctorId,
        String doctorName,
        Specialty specialty,
        String specialtyName,
        LocalDateTime dateTime,
        AppointmentStatus status,
        String reason) {

    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(appointment.getId(), appointment.getPatientId(),
                appointment.getDoctor().getId(), appointment.getDoctor().getFullName(),
                appointment.getDoctor().getSpecialty(), appointment.getDoctor().getSpecialty().getDisplayName(),
                appointment.getDateTime(), appointment.getStatus(), appointment.getReason());
    }
}
