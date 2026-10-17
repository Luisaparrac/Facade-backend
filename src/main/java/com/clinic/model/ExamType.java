package com.clinic.model;

import java.util.List;

public enum ExamType {
    CBC("Complete Blood Count", List.of(
            new LabParameter("Hemoglobin", "g/dL", 12.0, 17.5),
            new LabParameter("Hematocrit", "%", 36.0, 50.0),
            new LabParameter("White blood cells", "10^3/uL", 4.5, 11.0),
            new LabParameter("Platelets", "10^3/uL", 150.0, 450.0))),
    GLUCOSE("Fasting Glucose", List.of(
            new LabParameter("Glucose", "mg/dL", 70.0, 99.0))),
    LIPID_PROFILE("Lipid Profile", List.of(
            new LabParameter("Total cholesterol", "mg/dL", 125.0, 200.0),
            new LabParameter("LDL cholesterol", "mg/dL", 50.0, 100.0),
            new LabParameter("HDL cholesterol", "mg/dL", 40.0, 80.0),
            new LabParameter("Triglycerides", "mg/dL", 50.0, 150.0)));

    private final String displayName;
    private final List<LabParameter> parameters;

    ExamType(String displayName, List<LabParameter> parameters) {
        this.displayName = displayName;
        this.parameters = parameters;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<LabParameter> getParameters() {
        return parameters;
    }
}
