package com.my.conferences.controllers;

import com.my.conferences.db.DBException;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;
import com.my.conferences.logic.EventManager;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

@WebServlet(value = "/event")
public class EventServlet extends HttpServlet {

    private static final EventManager eventManager = EventManager.getInstance();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int id;
        try {
            id = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Expected 'id' should be integer");
            return;
        }

        Event event;
        try {
            event = eventManager.findOne(id, ((User) request.getSession().getAttribute("user")).getRole() != User.Role.USER);
            request.setAttribute("event", event);
        } catch (DBException e) {
            response.setStatus(404);
            response.getWriter().println(e.getMessage());
            return;
        }

        User user = (User) request.getSession().getAttribute("user");
        boolean isParticipant = false;
        if (!user.equals(event.getModerator())) {
            for (User participant : event.getParticipants())
                if (participant.equals(user)) {
                    isParticipant = true;
                    break;
                }
            request.setAttribute("isParticipant", isParticipant);
        }
        if (user.getRole() == User.Role.SPEAKER) {
            boolean hasReport = false;
            for (Report report : event.getReports())
                if (report.getSpeaker().equals(user)) {
                    hasReport = true;
                    break;
                }
            System.out.println(hasReport);
            request.setAttribute("hasReport", hasReport);
        }

        getServletContext().getRequestDispatcher("/event.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
