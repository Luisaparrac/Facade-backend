package com.clinic.exception;

import java.util.List;

public class AllergyConflictException extends ConflictException {

    private static final long serialVersionUID = 1L;

    public AllergyConflictException(List<String> conflicts) {
        super("The prescription was rejected because it conflicts with the patient's allergies.", conflicts);
    }
}
