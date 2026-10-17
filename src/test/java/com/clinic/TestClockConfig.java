package com.clinic;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

@TestConfiguration
public class TestClockConfig {

    public static final ZoneId ZONE = ZoneId.of("America/Bogota");
    public static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 19, 7, 0);

    @Bean
    @Primary
    public Clock fixedClock() {
        return Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);
    }
}
