package com.my.conferences.validation;

import com.my.conferences.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReportValidationTest {

    @Test
    void validateTopic() throws ValidationException {
        ReportValidation.validateTopic("top");
    }

    @Test
    void validateWrongTopic() {
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> ReportValidation.validateTopic(" to "),
                "Expected exception");
        assertEquals("Topic length min: 3", thrown.getMessage());
    }
}