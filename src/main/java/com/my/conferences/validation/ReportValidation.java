package com.my.conferences.validation;

import com.my.conferences.exception.ValidationException;

public class ReportValidation {

    private ReportValidation() {}

    public static void validateTopic(String topic) throws ValidationException {
        if (topic.trim().length() < 3) {
            throw new ValidationException("Topic length min: 3");
        }
    }
}
