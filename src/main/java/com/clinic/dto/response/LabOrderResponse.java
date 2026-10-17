package com.clinic.dto.response;

import com.clinic.model.ExamType;
import com.clinic.model.LabOrder;
import com.clinic.model.LabResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record LabOrderResponse(
        Long id,
        Long patientId,
        LocalDateTime requestedAt,
        LocalDateTime completedAt,
        long outOfRangeCount,
        List<ExamResultResponse> exams) {

    public static LabOrderResponse from(LabOrder order) {
        Map<ExamType, List<LabParameterResponse>> byExam = new LinkedHashMap<>();
        for (LabResult result : order.getResults()) {
            byExam.computeIfAbsent(result.getExamType(), exam -> new ArrayList<>()).add(LabParameterResponse.from(result));
        }
        List<ExamResultResponse> exams = byExam.entrySet().stream()
                .map(entry -> new ExamResultResponse(entry.getKey(), entry.getKey().getDisplayName(), entry.getValue()))
                .toList();
        return new LabOrderResponse(order.getId(), order.getPatientId(), order.getRequestedAt(),
                order.getCompletedAt(), order.countOutOfRange(), exams);
    }
}
