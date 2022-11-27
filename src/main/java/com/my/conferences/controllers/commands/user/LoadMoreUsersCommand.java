package com.my.conferences.controllers.commands.user;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.entity.User;
import com.my.conferences.exception.DBException;
import com.my.conferences.exception.ValidationException;
import com.my.conferences.service.UserService;
import com.my.conferences.util.JsonUtil;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class LoadMoreUsersCommand implements Command {

    private static final String EXCEPTION_MESSAGE = "Exception in SearchAvailableSpeakersCommand";
    private static final Logger logger = Logger.getLogger(LoadMoreUsersCommand.class);
    private final UserService userService;

    public LoadMoreUsersCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int page = RequestUtil.getIntParameter(request, "page");
            String emailQuery = RequestUtil.getStringParameter(request, "email_query");
            logger.trace("Page:  " + page);
            logger.trace("Email query: " + emailQuery);
            List<User> users = userService.findAll(emailQuery, page, RequestUtil.getUser(request));
            if (users.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            response.getWriter().println(JsonUtil.usersToJson(users));
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
