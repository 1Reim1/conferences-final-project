package com.my.conferences.validation;

import com.my.conferences.exception.ValidationException;

import java.util.Date;

public class EventValidation {

    private static final int TITLE_MIN_LENGTH = 3;
    private static final int DESCRIPTION_MIN_LENGTH = 20;
    private static final int PLACE_MIN_LENGTH = 5;

    private EventValidation() {}

    public static void validateTitle(String title) throws ValidationException {
        if (title.trim().length() < TITLE_MIN_LENGTH) {
            throw new ValidationException(String.format("Title min length: %d", TITLE_MIN_LENGTH));
        }
    }

    public static void validateDescription(String description) throws ValidationException {
        if (description.trim().length() < DESCRIPTION_MIN_LENGTH) {
            throw new ValidationException(String.format("Description min length: %d", DESCRIPTION_MIN_LENGTH));
        }
    }

    public static void validateDate(Date date) throws ValidationException {
        if (date.compareTo(new Date()) < 0) {
            throw new ValidationException("Required future date");
        }
    }

    public static void validatePlace(String place) throws ValidationException {
        if (place.trim().length() < PLACE_MIN_LENGTH) {
            throw new ValidationException(String.format("Place min length: %d", PLACE_MIN_LENGTH));
        }
    }
}
