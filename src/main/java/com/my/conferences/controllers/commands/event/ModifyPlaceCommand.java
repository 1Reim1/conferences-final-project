package com.my.conferences.controllers.commands.event;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.exception.DBException;
import com.my.conferences.exception.ValidationException;
import com.my.conferences.service.EventService;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import java.io.IOException;

public class ModifyPlaceCommand implements Command {

    private static final String EXCEPTION_MESSAGE = "Exception in ModifyPlaceCommand";
    private static final Logger logger = Logger.getLogger(ModifyPlaceCommand.class);
    private final EventService eventService;

    public ModifyPlaceCommand(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int eventId = RequestUtil.getIntParameter(request, "event_id");
            String place = RequestUtil.getStringParameter(request, "place");
            logger.trace("Event id: " + eventId);
            logger.trace("Place: " + place);
            eventService.modifyPlace(eventId, place, RequestUtil.getUser(request));
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
