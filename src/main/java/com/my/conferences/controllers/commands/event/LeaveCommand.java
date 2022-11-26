package com.my.conferences.controllers.commands.event;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.exception.DBException;
import com.my.conferences.service.EventService;
import com.my.conferences.exception.ValidationException;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import java.io.IOException;

public class LeaveCommand implements Command {

    private final static String EXCEPTION_MESSAGE = "Exception in LeaveCommand";
    private final static Logger logger = Logger.getLogger(LeaveCommand.class);
    private final EventService eventService;

    public LeaveCommand(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int eventId = RequestUtil.getIntParameter(request, "event_id");
            logger.trace("Event id: " + eventId);
            eventService.leave(eventId, RequestUtil.getUser(request));
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
