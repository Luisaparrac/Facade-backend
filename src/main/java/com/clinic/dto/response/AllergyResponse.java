package com.clinic.dto.response;

import com.clinic.model.Allergy;

public record AllergyResponse(Long id, String substance, String reaction) {

    public static AllergyResponse from(Allergy allergy) {
        return new AllergyResponse(allergy.getId(), allergy.getSubstance(), allergy.getReaction());
    }
}
