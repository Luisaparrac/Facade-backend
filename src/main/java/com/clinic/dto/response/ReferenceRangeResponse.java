package com.clinic.dto.response;

import com.clinic.model.LabParameter;

public record ReferenceRangeResponse(String name, String unit, double referenceMin, double referenceMax) {

    public static ReferenceRangeResponse from(LabParameter parameter) {
        return new ReferenceRangeResponse(parameter.name(), parameter.unit(), parameter.referenceMin(), parameter.referenceMax());
    }
}
