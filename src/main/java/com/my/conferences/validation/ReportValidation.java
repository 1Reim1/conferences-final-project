package com.my.conferences.validation;

import com.my.conferences.exception.ValidationException;

public class ReportValidation {

    private static final int TOPIC_MIN_LENGTH = 3;

    private ReportValidation() {}

    public static void validateTopic(String topic) throws ValidationException {
        if (topic.trim().length() < TOPIC_MIN_LENGTH) {
            throw new ValidationException(String.format("Topic length min: %d", TOPIC_MIN_LENGTH));
        }
    }
}
