package com.clinic.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResultStatusTest {

    @Test
    void classifiesValuesAgainstTheReferenceRange() {
        assertEquals(ResultStatus.LOW, ResultStatus.evaluate(65.0, 70.0, 99.0));
        assertEquals(ResultStatus.NORMAL, ResultStatus.evaluate(70.0, 70.0, 99.0));
        assertEquals(ResultStatus.NORMAL, ResultStatus.evaluate(99.0, 70.0, 99.0));
        assertEquals(ResultStatus.HIGH, ResultStatus.evaluate(126.0, 70.0, 99.0));
    }
}
