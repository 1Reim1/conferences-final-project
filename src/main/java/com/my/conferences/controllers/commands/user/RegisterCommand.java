package com.my.conferences.controllers.commands.user;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.db.DBException;
import com.my.conferences.entity.User;
import com.my.conferences.logic.UserManager;
import com.my.conferences.logic.ValidationException;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class RegisterCommand implements Command {
    private static final UserManager userManager = UserManager.getInstance();

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            User user = new User();
            user.setEmail(RequestUtil.getStringParameter(request, "email"));
            user.setFirstName(RequestUtil.getStringParameter(request, "first_name"));
            user.setLastName(RequestUtil.getStringParameter(request, "last_name"));
            user.setPassword(RequestUtil.getStringParameter(request, "password"));
            String role = RequestUtil.getStringParameter(request, "role").toUpperCase();

            if (!role.equals(User.Role.USER.toString()) && !role.equals(User.Role.SPEAKER.toString())) {
                role = User.Role.USER.toString();
            }

            user.setRole(User.Role.valueOf(role));
            user.setLanguage(RequestUtil.getCookiesMap(request).getOrDefault("lang", "en"));
            userManager.register(user);

            request.getSession().setAttribute("user", user);
        } catch (ValidationException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
        } catch (DBException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
        }
    }
}
