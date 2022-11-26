package com.my.conferences.validation;

import com.my.conferences.exception.ValidationException;

import java.util.regex.Pattern;

public class UserValidation {

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern VALID_NAME_REGEX = Pattern.compile("^[a-zA-Z]{3,}$");

    private UserValidation() {}

    public static void validateNames(String firstName, String lastName) throws ValidationException {
        if (!VALID_NAME_REGEX.matcher(firstName).find()) {
            throw new ValidationException("First name is bad");
        }
        if (!VALID_NAME_REGEX.matcher(lastName).find()) {
            throw new ValidationException("Last name is bad");
        }
    }

    public static void validateEmail(String email) throws ValidationException {
        if (!VALID_EMAIL_ADDRESS_REGEX.matcher(email).find()) {
            throw new ValidationException("Email is incorrect");
        }
    }

    public static void validateEmailAndPassword(String email, String password) throws ValidationException {
        validateEmail(email);
        if (password.length() < 6) {
            throw new ValidationException("Password is bad");
        }
    }

    public static String validateLanguage(String language) {
        if (language == null || (!language.equals("en") && !language.equals("uk"))) {
            return "en";
        }

        return language;
    }

}
