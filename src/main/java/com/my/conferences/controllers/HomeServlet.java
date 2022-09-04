package com.my.conferences.controllers;

import com.my.conferences.db.DBException;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.User;
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
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        request.setAttribute("user", user);

        int page;
        try {
            page = Integer.parseInt(request.getParameter("page"));
            if (page < 1)
                page = 1;
        }   catch (NumberFormatException e) {
            page = 1;
        }

        Cookie[] cookies = request.getCookies();
        Event.Order order = Event.Order.DATE;
        try {
            for (Cookie cookie : cookies) {
                System.out.println(cookie.getName());
                if (cookie.getName().equals("event-order"))
                    order = Event.Order.valueOf(cookie.getValue());
            }
        }   catch (IllegalArgumentException e) {}

        try {
            request.setAttribute("events", eventManager.findAll(page, order));
            request.setAttribute("pages", eventManager.countPages());
        } catch (DBException e) {
            response.setStatus(404);
            response.getWriter().println(e.getMessage());
            return;
        }

        request.setAttribute("order", order);
        request.setAttribute("page", page);
        getServletContext().getRequestDispatcher("/home.jsp").forward(request, response);
    }
}