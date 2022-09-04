package com.my.conferences.controllers;

import com.my.conferences.db.DBException;
import com.my.conferences.entity.Event;
import com.my.conferences.logic.EventManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private static final EventManager eventManager = EventManager.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        int page;
        try {
            page = Integer.parseInt(request.getParameter("page"));
            if (page < 1)
                page = 1;
        } catch (NumberFormatException e) {
            page = 1;
        }

        Cookie[] cookies = request.getCookies();
        Event.Order order = Event.Order.DATE;
        boolean reverseOrder = false;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("event-order")) {
                try {
                    order = Event.Order.valueOf(cookie.getValue());
                }   catch (IllegalArgumentException ignored) {}
            }
            if (cookie.getName().equals("event-order-reverse"))
                reverseOrder = true;
        }

        int pages;
        try {
            pages = eventManager.countPages();
            if (page > pages)
                page = pages;
            request.setAttribute("events", eventManager.findAll(page, order, reverseOrder));
        } catch (DBException e) {
            response.setStatus(404);
            response.getWriter().println(e.getMessage());
            return;
        }

        request.setAttribute("page", page);
        request.setAttribute("pages", pages);
        request.setAttribute("order", order);
        request.setAttribute("reverseOrder", reverseOrder);
        getServletContext().getRequestDispatcher("/home.jsp").forward(request, response);
    }
}