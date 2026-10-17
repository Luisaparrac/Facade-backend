package com.clinic.repository;

import com.clinic.model.SessionToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface SessionTokenRepository extends JpaRepository<SessionToken, String> {

    void deleteByExpiresAtBefore(LocalDateTime moment);
}
