package com.my.conferences.util;

import com.my.conferences.logic.ValidationException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

public class RequestUtil {

    private RequestUtil() {}

    public static String getStringParameter(HttpServletRequest request, String parameterName) throws ValidationException {
        String parameter = request.getParameter(parameterName);
        if (parameter == null)
            throw new ValidationException(String.format("Expected parameter '%s'", parameterName));

        return parameter;
    }

    public static int getIntParameter(HttpServletRequest request, String parameterName) throws ValidationException {
        String parameterStr = getStringParameter(request, parameterName);
        int parameter;
        try {
            parameter = Integer.parseInt(parameterStr);
        } catch (NumberFormatException e) {
            throw new ValidationException(String.format("Expected '%s' should be integer", parameterName));
        }

        return parameter;
    }

    public static long getLongParameter(HttpServletRequest request, String parameterName) throws ValidationException {
        String parameterStr = getStringParameter(request, parameterName);
        long parameter;
        try {
            parameter = Long.parseLong(parameterStr);
        } catch (NumberFormatException e) {
            throw new ValidationException(String.format("Expected '%s' should be integer", parameterName));
        }

        return parameter;
    }

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
