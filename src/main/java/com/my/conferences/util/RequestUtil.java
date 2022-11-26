package com.my.conferences.util;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.entity.User;
import com.my.conferences.exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Util class for comfortable work
 * with request parameters and cookies
 */
public class RequestUtil {

    private RequestUtil() {}

    /**
     * @return user in session
     */
    public static User getUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute("user");
    }

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
            throw new ValidationException(String.format("Expected '%s' should be long", parameterName));
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

    public static void executeCommand(HttpServletRequest request, HttpServletResponse response, Map<String, Command> commandMap) throws IOException, ServletException {
        String commandKey;
        try {
            commandKey = RequestUtil.getStringParameter(request, "command");
        } catch (ValidationException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
            return;
        }

        Command command = commandMap.get(commandKey);
        if (command == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().printf("Command '%s' is unknown.", commandKey);
            return;
        }

        command.execute(request, response);
    }
}
