package com.clinic.security;

import com.clinic.exception.ForbiddenException;
import com.clinic.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;

public final class AuthenticatedPatient {

    private static final String ATTRIBUTE = "authenticatedPatientId";

    private AuthenticatedPatient() {
    }

    static void set(HttpServletRequest request, Long patientId) {
        request.setAttribute(ATTRIBUTE, patientId);
    }

    public static Long get(HttpServletRequest request) {
        Object patientId = request.getAttribute(ATTRIBUTE);
        if (patientId == null) {
            throw new UnauthorizedException("You must log in to access this resource.");
        }
        return (Long) patientId;
    }

    public static void requireSelf(HttpServletRequest request, Long patientId) {
        if (!get(request).equals(patientId)) {
            throw new ForbiddenException("You can only access your own clinical information.");
        }
    }
}
