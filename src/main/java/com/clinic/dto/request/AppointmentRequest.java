package com.clinic.dto.request;

import com.clinic.model.Specialty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AppointmentRequest(
        @NotNull(message = "is required") Long patientId,
        @NotNull(message = "is required") Specialty specialty,
        @NotNull(message = "is required") LocalDateTime dateTime,
        Long doctorId,
        @Size(max = 200, message = "must have at most 200 characters") String reason) {
}
