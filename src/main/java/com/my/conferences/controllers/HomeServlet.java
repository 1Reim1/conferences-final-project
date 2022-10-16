package com.my.conferences.controllers;

import com.my.conferences.service.DBException;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.User;
import com.my.conferences.service.EventService;
import com.my.conferences.service.ValidationException;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private final static Logger logger = Logger.getLogger(HomeServlet.class);
    private EventService eventService;

    @Override
    public void init() {
        eventService = (EventService) getServletContext().getAttribute("app/eventService");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        User user = (User) request.getSession().getAttribute("user");
        int page;
        try {
            page = Math.max(RequestUtil.getIntParameter(request, "page"), 1);
        } catch (ValidationException e) {
            page = 1;
        }

        Map<String, String> cookiesMap = RequestUtil.getCookiesMap(request);
        boolean reverseOrder = cookiesMap.containsKey("event-order-reverse");
        boolean futureOrder = !cookiesMap.containsKey("event-order-time-past");
        boolean onlyMyEvents = cookiesMap.containsKey("event-order-my-events");

        Event.Order order = Event.Order.DATE;
        String orderString = cookiesMap.getOrDefault("event-order", "DATE");
        if (Arrays.stream(Event.Order.values()).anyMatch(o -> orderString.equals(o.toString()))) {
            order = Event.Order.valueOf(orderString);
        }

        int pages;
        try {
            pages = eventService.countPages(futureOrder, onlyMyEvents, user);
            page = Math.min(page, pages);
            request.setAttribute("events", eventService.findAll(page, order, reverseOrder, futureOrder, onlyMyEvents, user));
        } catch (DBException e) {
            logger.error("doGet: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
            return;
        }

        request.setAttribute("page", page);
        request.setAttribute("pages", pages);
        request.setAttribute("order", order);
        request.setAttribute("reverseOrder", reverseOrder);
        request.setAttribute("futureOrder", futureOrder);
        request.setAttribute("onlyMyEvents", onlyMyEvents);
        getServletContext().getRequestDispatcher("/WEB-INF/jsp/home.jsp").forward(request, response);
    }
}