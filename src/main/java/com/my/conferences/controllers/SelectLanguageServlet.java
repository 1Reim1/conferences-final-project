package com.my.conferences.controllers;

import com.my.conferences.db.DBException;
import com.my.conferences.entity.User;
import com.my.conferences.logic.UserManager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(value = "/select-language")
public class SelectLanguageServlet extends HttpServlet {
    private static final UserManager userManager = UserManager.getInstance();

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().println(((User) request.getSession().getAttribute("user")).getLanguage());
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            userManager.setLanguage((User) request.getSession().getAttribute("user"), request.getParameter("language"));
        } catch (DBException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
        }
    }
}