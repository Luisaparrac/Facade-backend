package com.clinic.dto.response;

import com.clinic.model.ExamType;

import java.util.List;

public record ExamResultResponse(ExamType exam, String examName, List<LabParameterResponse> parameters) {
}
