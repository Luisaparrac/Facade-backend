package com.clinic.service;

import com.clinic.model.LabParameter;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class LabResultSimulator {

    private static final double IN_RANGE_PROBABILITY = 0.8;

    private final Random random = new Random();

    public double simulate(LabParameter parameter) {
        double min = parameter.referenceMin();
        double max = parameter.referenceMax();
        double span = max - min;
        double value;
        if (random.nextDouble() < IN_RANGE_PROBABILITY) {
            value = min + random.nextDouble() * span;
        } else if (random.nextBoolean()) {
            value = max + span * (0.05 + random.nextDouble() * 0.25);
        } else {
            value = min - span * (0.05 + random.nextDouble() * 0.25);
        }
        return Math.round(Math.max(value, 0.1) * 10.0) / 10.0;
    }
}
