package com.my.conferences.controllers.commands.user;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.db.DBException;
import com.my.conferences.entity.User;
import com.my.conferences.logic.UserManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class SearchAvailableSpeakersCommand implements Command {
    private static final UserManager userManager = UserManager.getInstance();
    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String searchQuery = request.getParameter("searchQuery");

        int eventId;
        try {
            eventId = Integer.parseInt(request.getParameter("eventId"));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Expected 'eventId' should be integer");
            return;
        }

        List<User> speakers;
        try {
            speakers = userManager.searchAvailableSpeakers(eventId, searchQuery, (User) request.getSession().getAttribute("user"));
        } catch (DBException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.out.println(e.getMessage());
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
        jsonResponse.deleteCharAt(jsonResponse.length()-1).append("]");
        response.getWriter().println(jsonResponse);
    }
}