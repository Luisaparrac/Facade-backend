package com.clinic.dto.response;

import com.clinic.model.Specialty;

public record SpecialtyResponse(Specialty code, String name) {

    public static SpecialtyResponse from(Specialty specialty) {
        return new SpecialtyResponse(specialty, specialty.getDisplayName());
    }
}
