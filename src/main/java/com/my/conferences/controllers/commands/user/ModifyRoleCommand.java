package com.my.conferences.controllers.commands.user;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.entity.User;
import com.my.conferences.service.DBException;
import com.my.conferences.service.UserService;
import com.my.conferences.service.ValidationException;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;

public class ModifyRoleCommand implements Command {

    private final static String EXCEPTION_MESSAGE = "Exception in ModifyRoleCommand";
    private final static Logger logger = Logger.getLogger(ModifyRoleCommand.class);
    private final UserService userService;

    public ModifyRoleCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int user_id = RequestUtil.getIntParameter(request, "user_id");
            String newRoleStr = RequestUtil.getStringParameter(request, "new_role").toUpperCase();
            if (Arrays.stream(User.Role.values()).noneMatch(r -> r.toString().equals(newRoleStr))) {
                throw new ValidationException("Role is unknown");
            }
            User.Role newRole = User.Role.valueOf(newRoleStr);
            userService.modifyRole(user_id, newRole, (User) request.getSession().getAttribute("user"));
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
