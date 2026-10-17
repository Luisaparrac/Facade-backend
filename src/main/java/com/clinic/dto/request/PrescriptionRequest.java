package com.clinic.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PrescriptionRequest(
        @NotNull(message = "is required") Long patientId,
        @NotEmpty(message = "must contain at least one medication")
        @Size(max = 10, message = "must contain at most 10 medications")
        List<@NotNull(message = "cannot be null") @Valid MedicationRequest> medications,
        @Size(max = 200, message = "must have at most 200 characters") String diagnosis,
        Long doctorId) {
}
