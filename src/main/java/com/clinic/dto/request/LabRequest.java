package com.clinic.dto.request;

import com.clinic.model.ExamType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record LabRequest(
        @NotNull(message = "is required") Long patientId,
        @NotEmpty(message = "must contain at least one exam") List<@NotNull(message = "cannot be null") ExamType> exams) {
}
