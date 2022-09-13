package com.my.conferences.controllers;

import com.my.conferences.db.DBException;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.User;
import com.my.conferences.logic.EventManager;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.Date;

@WebServlet("/create-event")
public class CreateEventServlet extends HttpServlet {
    private static final EventManager eventManager = EventManager.getInstance();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getServletContext().getRequestDispatcher("/create-event.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Event event = new Event();
        event.setTitle(request.getParameter("title"));
        event.setDescription(request.getParameter("description"));
        event.setPlace(request.getParameter("place"));
        event.setModerator((User) request.getSession().getAttribute("user"));

        try {
            event.setDate(new Date(Long.parseLong(request.getParameter("date"))));
        }   catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Expected 'date' should be long");
            return;
        }

        try {
            eventManager.create(event);
        }   catch (DBException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
            return;
        }

        response.getWriter().println(event.getId());
    }
}
