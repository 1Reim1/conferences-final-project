package com.my.conferences.controllers;

import com.my.conferences.db.DBException;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.User;
import com.my.conferences.logic.EventManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

        Event.Order order = Event.Order.DATE;
        try {
            String o = request.getParameter("order");
            if (o != null)
                order = Event.Order.valueOf(o);
        }   catch (IllegalArgumentException e) {

        }

        try {
            request.setAttribute("events", eventManager.findAll(page));
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