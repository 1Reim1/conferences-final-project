package com.my.conferences.controllers.commands.event;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.entity.Event;
import com.my.conferences.exception.DBException;
import com.my.conferences.service.EventService;
import com.my.conferences.exception.ValidationException;
import com.my.conferences.util.JsonUtil;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class LoadModeratorEventsCommand implements Command {

    private final static String EXCEPTION_MESSAGE = "Exception in LoadModeratorEventsCommand";
    private final static Logger logger = Logger.getLogger(LoadModeratorEventsCommand.class);
    private final EventService eventService;

    public LoadModeratorEventsCommand(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int moderatorId = RequestUtil.getIntParameter(request, "moderator_id");
            logger.trace("Moderator id: " + moderatorId);
            List<Event> events = eventService.findAllModeratorEvents(moderatorId);
            response.getWriter().println(JsonUtil.eventsToJson(events));
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
