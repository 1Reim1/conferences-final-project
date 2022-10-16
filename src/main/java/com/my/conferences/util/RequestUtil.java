package com.my.conferences.util;

import com.my.conferences.service.ValidationException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Util class for comfortable work
 * with request parameters and cookies
 */
public class RequestUtil {

    private RequestUtil() {}

    /**
     * @param parameterName the name of the parameter
     * @return string value of parameter
     */
    public static String getStringParameter(HttpServletRequest request, String parameterName) throws ValidationException {
        String parameter = request.getParameter(parameterName);
        if (parameter == null)
            throw new ValidationException(String.format("Expected parameter '%s'", parameterName));

        return parameter;
    }

    /**
     * @param parameterName the name of the parameter
     * @return int value of parameter
     */
    public static int getIntParameter(HttpServletRequest request, String parameterName) throws ValidationException {
        int parameter;
        try {
            parameter = Integer.parseInt(getStringParameter(request, parameterName));
        } catch (NumberFormatException e) {
            throw new ValidationException(String.format("Expected '%s' should be integer", parameterName));
        }

        return parameter;
    }

    /**
     * @param parameterName the name of the parameter
     * @return long value of parameter
     */
    public static long getLongParameter(HttpServletRequest request, String parameterName) throws ValidationException {
        long parameter;
        try {
            parameter = Long.parseLong(getStringParameter(request, parameterName));
        } catch (NumberFormatException e) {
            throw new ValidationException(String.format("Expected '%s' should be integer", parameterName));
        }

        return parameter;
    }

    /**
     * Method for comfortable work with cookies
     * Returns the HashMap in which keys and values are strings
     *
     * @return HashMap of cookies
     */
    public static Map<String, String> getCookiesMap(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return new HashMap<>();

        Map<String, String> cookiesMap = new HashMap<>(cookies.length);
        for (Cookie cookie : cookies)
            cookiesMap.put(cookie.getName(), cookie.getValue());

        return cookiesMap;
    }
}
