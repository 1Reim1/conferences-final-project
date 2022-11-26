package com.my.conferences.controllers;

import com.my.conferences.exception.DBException;
import com.my.conferences.entity.Event;
import com.my.conferences.service.EventService;
import com.my.conferences.exception.ValidationException;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Date;

@WebServlet("/create-event")
public class CreateEventServlet extends HttpServlet {

    private final static String EXCEPTION_MESSAGE = "Exception in SelectLanguageServlet";
    private final static Logger logger = Logger.getLogger(CreateEventServlet.class);
    private EventService eventService;

    @Override
    public void init() {
        eventService = (EventService) getServletContext().getAttribute("app/eventService");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getServletContext().getRequestDispatcher("/WEB-INF/jsp/create-event.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Event event = new Event();
            event.setTitle(RequestUtil.getStringParameter(request, "title"));
            event.setDescription(RequestUtil.getStringParameter(request, "description"));
            event.setPlace(RequestUtil.getStringParameter(request, "place"));
            event.setModerator(RequestUtil.getUser(request));
            event.setDate(new Date(RequestUtil.getLongParameter(request, "date")));

            logger.trace("Title: " + event.getTitle());
            logger.trace("Description: " + event.getDescription());
            logger.trace("Place: " + event.getPlace());
            logger.trace("Date: " + event.getDate().toString());
            logger.trace("Moderator id: " + event.getModerator().getId());

            eventService.create(event);
            response.getWriter().println(event.getId());
        } catch (ValidationException e) {
            logger.error(EXCEPTION_MESSAGE, e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
        } catch (DBException e) {
            logger.error(EXCEPTION_MESSAGE, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
        }
    }
}
