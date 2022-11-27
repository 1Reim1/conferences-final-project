package com.my.conferences.validation;

import com.my.conferences.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserValidationTest {

    @Test
    void validateNames() throws ValidationException {
        UserValidation.validateNames("Reim", "Time");
    }

    @Test
    void validateNamesWithWrongFirstName() {
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> UserValidation.validateNames("R", "Time"),
                "Expected exception");
        assertEquals("First name is bad", thrown.getMessage());
    }

    @Test
    void validateNamesWithWrongLastName() {
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> UserValidation.validateNames("Reim", "Ti"),
                "Expected exception");
        assertEquals("Last name is bad", thrown.getMessage());
    }

    @Test
    void validateEmail() throws ValidationException {
        UserValidation.validateEmail("emailtest@gmail.com");
    }

    @Test
    void validateWrongEmail() {
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> UserValidation.validateEmail("emailtest@gmail.c"),
                "Expected exception");
        assertEquals("Email is incorrect", thrown.getMessage());
    }

    @Test
    void validateEmailAndPassword() throws ValidationException {
        UserValidation.validateEmailAndPassword("emailtest@gmail.com", "password");
    }

    @Test
    void validateEmailAndWrongPassword() {
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> UserValidation.validateEmailAndPassword("emailtest@gmail.com", "12345"),
                "Expected exception");
        assertEquals("Password is bad", thrown.getMessage());
    }

    @Test
    void validateLanguage() {
        String language = UserValidation.validateLanguage("en");
        assertEquals("en", language);
    }

    @Test
    void validateLanguageWithNull() {
        String language = UserValidation.validateLanguage(null);
        assertEquals("en", language);
    }

    @Test
    void validateWrongLanguage() {
        String language = UserValidation.validateLanguage("ru");
        assertEquals("en", language);
    }
}