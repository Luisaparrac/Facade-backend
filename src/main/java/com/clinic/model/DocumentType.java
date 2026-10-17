package com.clinic.model;

public enum DocumentType {
    CITIZEN_ID("Citizen ID (CC)"),
    IDENTITY_CARD("Identity card for minors (TI)"),
    FOREIGNER_ID("Foreigner ID (CE)"),
    PASSPORT("Passport (PA)");

    private final String displayName;

    DocumentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
