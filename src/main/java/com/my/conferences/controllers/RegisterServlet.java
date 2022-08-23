package com.my.conferences.controllers;

import com.my.conferences.db.DBException;
import com.my.conferences.entity.User;
import com.my.conferences.logic.UserManager;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final UserManager userManager = UserManager.getInstance();
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = new User();
        user.setEmail(request.getParameter("email"));
        user.setFirstName(request.getParameter("first_name"));
        user.setLastName(request.getParameter("last_name"));
        user.setPassHash(request.getParameter("password"));

        try {
            if (!user.getPassHash().equals(request.getParameter("password_repeated")))
                throw new DBException("Passwords do not match");
            userManager.register(user);
        } catch (DBException e) {
            e.printStackTrace();
            response.setStatus(404);
            response.getWriter().println(e.getMessage());
            return;
        }

        System.out.println(user);
    }
}
