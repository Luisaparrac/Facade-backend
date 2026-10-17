package com.clinic.dto.response;

import com.clinic.model.LabResult;
import com.clinic.model.ResultStatus;

public record LabParameterResponse(
        String name,
        double value,
        String unit,
        double referenceMin,
        double referenceMax,
        ResultStatus status,
        boolean withinRange) {

    public static LabParameterResponse from(LabResult result) {
        return new LabParameterResponse(result.getParameterName(), result.getResultValue(), result.getUnit(),
                result.getReferenceMin(), result.getReferenceMax(), result.getStatus(),
                result.getStatus() == ResultStatus.NORMAL);
    }
}
