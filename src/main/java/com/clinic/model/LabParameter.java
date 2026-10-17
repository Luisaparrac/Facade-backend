package com.clinic.model;

public record LabParameter(String name, String unit, double referenceMin, double referenceMax) {
}
