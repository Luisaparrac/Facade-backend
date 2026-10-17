package com.clinic.dto.response;

import com.clinic.model.ExamType;

import java.util.List;

public record ExamTypeResponse(ExamType code, String name, List<ReferenceRangeResponse> parameters) {

    public static ExamTypeResponse from(ExamType exam) {
        return new ExamTypeResponse(exam, exam.getDisplayName(),
                exam.getParameters().stream().map(ReferenceRangeResponse::from).toList());
    }
}
