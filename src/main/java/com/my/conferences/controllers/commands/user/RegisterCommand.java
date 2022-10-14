package com.my.conferences.controllers.commands.user;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.db.DBException;
import com.my.conferences.entity.User;
import com.my.conferences.logic.UserManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class RegisterCommand implements Command {
    private static final UserManager userManager = UserManager.getInstance();
    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = new User();
        user.setEmail(request.getParameter("email"));
        user.setFirstName(request.getParameter("first_name"));
        user.setLastName(request.getParameter("last_name"));
        user.setPassHash(request.getParameter("password"));
        user.setRole(User.Role.valueOf(request.getParameter("role").toUpperCase()));

        Cookie[] cookies = request.getCookies();
        Cookie langCookie = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("lang"))
                    langCookie = cookie;
            }
        }

        if (langCookie != null)
            user.setLanguage(langCookie.getValue());
        else
            user.setLanguage("en");

        try {
            userManager.register(user);
        } catch (DBException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
            return;
        }

        request.getSession().setAttribute("user", user);
    }
}
