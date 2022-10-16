package com.my.conferences.controllers.commands.user;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.service.DBException;
import com.my.conferences.entity.User;
import com.my.conferences.service.UserService;
import com.my.conferences.service.ValidationException;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import java.io.IOException;

public class RegisterCommand implements Command {

    private final static Logger logger = Logger.getLogger(RegisterCommand.class);
    private final UserService userService;

    public RegisterCommand(UserService userService) {
        this.userService = userService;
    }

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

            logger.debug("Email: " + user.getEmail());
            logger.debug("First name: " + user.getFirstName());
            logger.debug("Last name: " + user.getLastName());
            logger.debug("Role: " + role);
            logger.debug("Language: " + user.getLanguage());
            userService.register(user);

            request.getSession().setAttribute("user", user);
        } catch (ValidationException e) {
            logger.error("execute: ", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
        } catch (DBException e) {
            logger.error("execute: ", e);
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
        }
    }
}
