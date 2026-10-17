package com.clinic.model;

public enum ResultStatus {
    LOW,
    NORMAL,
    HIGH;

    public static ResultStatus evaluate(double value, double referenceMin, double referenceMax) {
        if (value < referenceMin) return LOW;
        if (value > referenceMax) return HIGH;
        return NORMAL;
    }
}
