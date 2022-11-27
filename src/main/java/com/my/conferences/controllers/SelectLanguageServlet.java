package com.my.conferences.controllers;

import com.my.conferences.exception.DBException;
import com.my.conferences.exception.ValidationException;
import com.my.conferences.service.UserService;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import java.io.IOException;

@WebServlet(value = "/select-language")
public class SelectLanguageServlet extends HttpServlet {

    private static final String EXCEPTION_MESSAGE = "Exception in SelectLanguageServlet";
    private static final Logger logger = Logger.getLogger(SelectLanguageServlet.class);
    private UserService userService;

    @Override
    public void init() {
        userService = (UserService) getServletContext().getAttribute("app/userService");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String language = RequestUtil.getStringParameter(request, "language");
            logger.trace("Language: " + language);
            userService.setLanguage(RequestUtil.getUser(request), language);
        } catch (ValidationException e) {
            logger.error(EXCEPTION_MESSAGE, e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
        } catch (DBException e) {
            logger.error(EXCEPTION_MESSAGE, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
        }
    }
}