package com.my.conferences.controllers;

import com.my.conferences.db.DBException;
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

        try {
            request.setAttribute("event", eventManager.findOne(id, ((User) request.getSession().getAttribute("user")).getRole() != User.Role.USER));
        } catch (DBException e) {
            response.setStatus(404);
            response.getWriter().println(e.getMessage());
            return;
        }

        getServletContext().getRequestDispatcher("/event.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
