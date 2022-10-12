package com.my.conferences.controllers;

import java.io.*;

import com.my.conferences.db.DBException;
import com.my.conferences.entity.User;
import com.my.conferences.logic.UserManager;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(value = "/login")
public class LoginServlet extends HttpServlet {
    private static final UserManager userManager = UserManager.getInstance();

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String language;

        Cookie[] cookies = request.getCookies();
        Cookie langCookie = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("lang"))
                    langCookie = cookie;
            }
        }

        if (langCookie != null)
            language = langCookie.getValue();
        else
            language = "en";

        User user;
        try {
            user = userManager.login(email, password, language);
        } catch (DBException e) {
            response.setStatus(404);
            response.getWriter().println(e.getMessage());
            return;
        }

        HttpSession session = request.getSession();
        session.setAttribute("user", user);
    }
}