package com.my.conferences.util;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.entity.User;
import com.my.conferences.service.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestUtilTest {

    private static HttpServletRequest request;

    @BeforeAll
    static void init() {
        request = Mockito.mock(HttpServletRequest.class);
        Mockito.doReturn("String")
                .when(request)
                .getParameter(ArgumentMatchers.eq("string"));
        Mockito.doReturn("1")
                .when(request)
                .getParameter(ArgumentMatchers.eq("int"));
        Mockito.doReturn("2")
                .when(request)
                .getParameter(ArgumentMatchers.eq("long"));

        Cookie cookie1 = new Cookie("name1", "value1");
        Cookie cookie2 = new Cookie("name2", "value2");
        Cookie cookie3 = new Cookie("name3", "value3");
        Mockito.doReturn(new Cookie[]{cookie1, cookie2, cookie3})
                .when(request)
                .getCookies();

        User user = new User();
        user.setId(10);
        HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.doReturn(session)
                .when(request)
                .getSession();
        Mockito.doReturn(user)
                .when(session)
                .getAttribute(ArgumentMatchers.eq("user"));
    }

    @Test
    void getUser() {
        User user = RequestUtil.getUser(request);
        assertEquals(10, user.getId());
    }

    @Test
    void getStringParameter() throws ValidationException {
        String result = RequestUtil.getStringParameter(request, "string");
        assertEquals("String", result);
    }

    @Test
    void getIntParameter() throws ValidationException {
        int result = RequestUtil.getIntParameter(request, "int");
        assertEquals(1, result);
        // test str
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> RequestUtil.getIntParameter(request, "string"),
                "Expected exception");
        assertEquals("Expected 'string' should be integer", thrown.getMessage());
    }

    @Test
    void getLongParameter() throws ValidationException {
        long result = RequestUtil.getLongParameter(request, "long");
        assertEquals(2, result);
        // test str
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> RequestUtil.getLongParameter(request, "string"),
                "Expected exception");
        assertEquals("Expected 'string' should be long", thrown.getMessage());
    }

    @Test
    void getCookiesMap() {
        Map<String, String> cookiesMap = RequestUtil.getCookiesMap(request);
        assertEquals(3, cookiesMap.size());
        assertEquals("value1", cookiesMap.get("name1"));
        assertEquals("value2", cookiesMap.get("name2"));
        assertEquals("value3", cookiesMap.get("name3"));
    }

    @Test
    void executeCommand() throws ServletException, IOException {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        Command command = Mockito.mock(Command.class);
        Command command2 = Mockito.mock(Command.class);

        Map<String, Command> commandMap = new HashMap<>();
        commandMap.put("command-name", command);
        commandMap.put("command-name-2", command2);

        Mockito.doReturn("command-name")
                .when(request)
                .getParameter(ArgumentMatchers.eq("command"));
        RequestUtil.executeCommand(request, response, commandMap);
        Mockito.verify(command, Mockito.times(1))
                .execute(ArgumentMatchers.same(request), ArgumentMatchers.same(response));
    }
}