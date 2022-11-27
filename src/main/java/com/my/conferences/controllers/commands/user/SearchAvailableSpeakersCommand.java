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

public class SearchAvailableSpeakersCommand implements Command {

    private static final String EXCEPTION_MESSAGE = "Exception in SearchAvailableSpeakersCommand";
    private static final Logger logger = Logger.getLogger(SearchAvailableSpeakersCommand.class);
    private final UserService userService;

    public SearchAvailableSpeakersCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<User> speakers;
        try {
            String searchQuery = RequestUtil.getStringParameter(request, "search_query");
            int eventId = RequestUtil.getIntParameter(request, "event_id");
            logger.trace("Search query: " + searchQuery);
            logger.trace("Event id: " + eventId);

            speakers = userService.searchAvailableSpeakers(eventId, searchQuery, RequestUtil.getUser(request));
        } catch (ValidationException e) {
            logger.error(EXCEPTION_MESSAGE, e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
            return;
        } catch (DBException e) {
            logger.error(EXCEPTION_MESSAGE, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
            return;
        }

        if (speakers.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.getWriter().println(JsonUtil.usersToJson(speakers));
    }
}
