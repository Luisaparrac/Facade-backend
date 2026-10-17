package com.clinic.controller;

import com.clinic.dto.request.LoginRequest;
import com.clinic.dto.response.LoginResponse;
import com.clinic.facade.ClinicFacade;
import com.clinic.security.PublicEndpoint;
import com.clinic.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clinic/auth")
public class AuthController {

    private final AuthService authService;
    private final ClinicFacade clinicFacade;

    public AuthController(AuthService authService, ClinicFacade clinicFacade) {
        this.authService = authService;
        this.clinicFacade = clinicFacade;
    }

    @PublicEndpoint
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        AuthService.LoginSession session = authService.login(request.documentNumber(), request.password());
        return new LoginResponse(session.token(), session.expiresAt(), clinicFacade.getPatientProfile(session.patientId()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        authService.logout(authorization.substring("Bearer ".length()).trim());
        return ResponseEntity.noContent().build();
    }
}
