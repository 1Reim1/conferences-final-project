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
import java.util.List;

public class SearchAvailableSpeakersCommand implements Command {

    private final static Logger logger = Logger.getLogger(SearchAvailableSpeakersCommand.class);
    private final UserService userService;

    public SearchAvailableSpeakersCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String searchQuery;
        int eventId;
        List<User> speakers;
        try {
            searchQuery = RequestUtil.getStringParameter(request, "search_query");
            eventId = RequestUtil.getIntParameter(request, "event_id");
            speakers = userService.searchAvailableSpeakers(eventId, searchQuery, (User) request.getSession().getAttribute("user"));
        } catch (ValidationException e) {
            logger.error("execute: ", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
            return;
        } catch (DBException e) {
            logger.error("execute: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
            return;
        }

        if (speakers.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        StringBuilder jsonResponse = new StringBuilder("[");
        for (User speaker : speakers) {
            jsonResponse.append("{")
                    .append("\"id\": ")
                    .append(speaker.getId())
                    .append(",\"firstName\": \"")
                    .append(speaker.getFirstName())
                    .append("\",\"lastName\": \"")
                    .append(speaker.getLastName())
                    .append("\",\"email\": \"")
                    .append(speaker.getEmail())
                    .append("\"},");
        }

        jsonResponse.deleteCharAt(jsonResponse.length() - 1).append("]");
        response.getWriter().println(jsonResponse);
    }
}
