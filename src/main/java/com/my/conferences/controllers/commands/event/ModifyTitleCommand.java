package com.my.conferences.controllers.commands.event;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.service.DBException;
import com.my.conferences.service.EventService;
import com.my.conferences.service.ValidationException;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import java.io.IOException;

public class ModifyTitleCommand implements Command {

    private final static String EXCEPTION_MESSAGE = "Exception in ModifyTitleCommand";
    private final static Logger logger = Logger.getLogger(ModifyTitleCommand.class);
    private final EventService eventService;

    public ModifyTitleCommand(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int eventId = RequestUtil.getIntParameter(request, "event_id");
            String title = RequestUtil.getStringParameter(request, "title");
            logger.trace("Event id: " + eventId);
            logger.trace("Title: " + title);
            eventService.modifyTitle(eventId, title, RequestUtil.getUser(request));
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
