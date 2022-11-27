package com.my.conferences.controllers.commands.user;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.entity.User;
import com.my.conferences.exception.DBException;
import com.my.conferences.exception.ValidationException;
import com.my.conferences.service.UserService;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import java.io.IOException;

public class ModifyPasswordCommand implements Command {

    private static final String EXCEPTION_MESSAGE = "Exception in ModifyPasswordCommand";
    private static final Logger logger = Logger.getLogger(ModifyPasswordCommand.class);
    private final UserService userService;

    public ModifyPasswordCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String email = RequestUtil.getStringParameter(request, "email");
            String code = RequestUtil.getStringParameter(request, "code");
            String newPassword = RequestUtil.getStringParameter(request, "new_password");
            logger.trace("Email: " + email);
            logger.trace("Code: " + code);
            User user = userService.modifyPassword(email, code, newPassword);
            request.getSession().setAttribute("user", user);
        } catch (ValidationException e) {
            logger.error(EXCEPTION_MESSAGE, e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
        } catch (DBException e) {
            logger.error(EXCEPTION_MESSAGE, e);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println(e.getMessage());
        }
    }
}
