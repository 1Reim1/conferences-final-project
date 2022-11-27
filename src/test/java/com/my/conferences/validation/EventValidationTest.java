package com.my.conferences.validation;

import com.my.conferences.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventValidationTest {

    @Test
    void validateTitle() throws ValidationException {
        EventValidation.validateTitle("Tit");
    }

    @Test
    void validateWrongTitle() {
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> EventValidation.validateTitle(" Ti "),
                "Expected exception");
        assertEquals("Title min length: 3", thrown.getMessage());
    }

    @Test
    void validateDescription() throws ValidationException {
        EventValidation.validateDescription("Description bla blab");
    }

    @Test
    void validateWrongDescription() {
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> EventValidation.validateDescription(" Description bla bla "),
                "Expected exception");
        assertEquals("Description min length: 20", thrown.getMessage());
    }

    @Test
    void validateDate() throws ValidationException {
        EventValidation.validateDate(new Date(System.currentTimeMillis() + 10000));
    }

    @Test
    void validatePastDate() {
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> EventValidation.validateDate(new Date(System.currentTimeMillis() - 1000)),
                "Expected exception");
        assertEquals("Required future date", thrown.getMessage());
    }

    @Test
    void validatePlace() throws ValidationException {
        EventValidation.validatePlace("place");
    }

    @Test
    void validateWrongPlace() {
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> EventValidation.validatePlace(" rock "),
                "Expected exception");
        assertEquals("Place min length: 5", thrown.getMessage());
    }
}