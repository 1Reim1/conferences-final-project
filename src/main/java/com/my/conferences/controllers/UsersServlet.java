package com.my.conferences.controllers;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.controllers.commands.event.LoadModeratorEventsCommand;
import com.my.conferences.controllers.commands.report.LoadSpeakerFutureReportsCommand;
import com.my.conferences.controllers.commands.user.LoadMoreUsersCommand;
import com.my.conferences.controllers.commands.user.ModifyRoleCommand;
import com.my.conferences.entity.User;
import com.my.conferences.exception.DBException;
import com.my.conferences.exception.ValidationException;
import com.my.conferences.service.EventService;
import com.my.conferences.service.ReportService;
import com.my.conferences.service.UserService;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(value = "/users")
public class UsersServlet extends HttpServlet {

    private static final String EXCEPTION_MESSAGE = "Exception in UsersServlet";
    private static final Logger logger = Logger.getLogger(UsersServlet.class);
    private UserService userService;
    private static final Map<String, Command> commandMap = new HashMap<>();

    @Override
    public void init() {
        userService = (UserService) getServletContext().getAttribute("app/userService");
        EventService eventService = (EventService) getServletContext().getAttribute("app/eventService");
        ReportService reportService = (ReportService) getServletContext().getAttribute("app/reportService");
        commandMap.put("load-more-users", new LoadMoreUsersCommand(userService));
        commandMap.put("load-moderator-events", new LoadModeratorEventsCommand(eventService));
        commandMap.put("load-speaker-future-reports", new LoadSpeakerFutureReportsCommand(reportService));
        commandMap.put("modify-role", new ModifyRoleCommand(userService));
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<User> users = userService.findAll("", 1, RequestUtil.getUser(request));
            request.setAttribute("users", users);
        } catch (DBException | ValidationException e) {
            logger.error(EXCEPTION_MESSAGE, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
            return;
        }

        getServletContext().getRequestDispatcher("/WEB-INF/jsp/users.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.debug("Users post request");
        RequestUtil.executeCommand(request, response, commandMap);
    }
}