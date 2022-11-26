package com.my.conferences.validation;

import com.my.conferences.exception.ValidationException;

import java.util.Date;

public class EventValidation {

    private EventValidation() {}

    public static void validateTitle(String title) throws ValidationException {
        if (title.trim().length() < 3) {
            throw new ValidationException("Title min length: 3");
        }
    }

    public static void validateDescription(String description) throws ValidationException {
        if (description.trim().length() < 20) {
            throw new ValidationException("Description min length: 20");
        }
    }

    public static void validateDate(Date date) throws ValidationException {
        if (date.compareTo(new Date()) < 0) {
            throw new ValidationException("Required future date");
        }
    }

    public static void validatePlace(String place) throws ValidationException {
        if (place.trim().length() < 5) {
            throw new ValidationException("Place min length: 5");
        }
    }
}
