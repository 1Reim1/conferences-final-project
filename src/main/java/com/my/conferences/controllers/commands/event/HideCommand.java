package com.my.conferences.controllers.commands.event;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.service.DBException;
import com.my.conferences.entity.User;
import com.my.conferences.service.EventService;
import com.my.conferences.service.ValidationException;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import java.io.IOException;

public class HideCommand implements Command {

    private final static Logger logger = Logger.getLogger(HideCommand.class);
    private final EventService eventService;

    public HideCommand(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int eventId = RequestUtil.getIntParameter(request, "event_id");
            logger.debug("Event id: " + eventId);
            eventService.hide(eventId, (User) request.getSession().getAttribute("user"));
        } catch (ValidationException e) {
            logger.error("execute: ", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
        } catch (DBException e) {
            logger.error("execute: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
        }
    }
}
