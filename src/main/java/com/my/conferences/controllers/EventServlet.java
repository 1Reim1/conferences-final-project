package com.my.conferences.controllers;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.controllers.commands.event.*;
import com.my.conferences.controllers.commands.report.CancelCommand;
import com.my.conferences.controllers.commands.report.ConfirmCommand;
import com.my.conferences.controllers.commands.report.ModifyTopicCommand;
import com.my.conferences.controllers.commands.report.OfferCommand;
import com.my.conferences.controllers.commands.user.SearchAvailableSpeakersCommand;
import com.my.conferences.service.DBException;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.User;
import com.my.conferences.service.EventService;
import com.my.conferences.service.ReportService;
import com.my.conferences.service.UserService;
import com.my.conferences.service.ValidationException;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@WebServlet(value = "/event")
public class EventServlet extends HttpServlet {

    private final static String EXCEPTION_MESSAGE = "Exception in EventServlet";
    private final static Logger logger = Logger.getLogger(EventServlet.class);
    private final Map<String, Command> commandMap = new HashMap<>();
    private EventService eventService;

    @Override
    public void init() {
        eventService = (EventService) getServletContext().getAttribute("app/eventService");
        ReportService reportService = (ReportService) getServletContext().getAttribute("app/reportService");
        UserService userService = (UserService) getServletContext().getAttribute("app/userService");

        commandMap.put("join", new JoinCommand(eventService));
        commandMap.put("leave", new LeaveCommand(eventService));
        commandMap.put("hide", new HideCommand(eventService));
        commandMap.put("show", new ShowCommand(eventService));
        commandMap.put("modify-title", new ModifyTitleCommand(eventService));
        commandMap.put("modify-description", new ModifyDescriptionCommand(eventService));
        commandMap.put("modify-date", new ModifyDateCommand(eventService));
        commandMap.put("modify-place", new ModifyPlaceCommand(eventService));
        commandMap.put("modify-statistics", new ModifyStatisticsCommand(eventService));
        commandMap.put("cancel-report", new CancelCommand(reportService));
        commandMap.put("confirm-report", new ConfirmCommand(reportService));
        commandMap.put("offer-report", new OfferCommand(reportService));
        commandMap.put("modify-report-topic", new ModifyTopicCommand(reportService));
        commandMap.put("search-speaker", new SearchAvailableSpeakersCommand(userService));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        Event event;
        try {
            int id = RequestUtil.getIntParameter(request, "id");
            logger.trace("Id: " + id);
            event = eventService.findOne(id, user);
        } catch (ValidationException e) {
            logger.error(EXCEPTION_MESSAGE, e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
            return;
        } catch (DBException e) {
            logger.error(EXCEPTION_MESSAGE, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
            return;
        }

        boolean isModerator = event.getModerator().equals(user);
        boolean isFutureEvent = event.getDate().compareTo(new Date()) > 0;
        boolean isParticipant = false;
        boolean hasReport = false;
        if (!isModerator) {
            isParticipant = event.getParticipants().contains(user);
        }
        if (user.getRole() == User.Role.SPEAKER) {
            hasReport = event.getReports().stream().anyMatch(r -> r.getSpeaker().equals(user));
        }

        request.setAttribute("event", event);
        request.setAttribute("isModerator", isModerator);
        request.setAttribute("isFutureEvent", isFutureEvent);
        request.setAttribute("isParticipant", isParticipant);
        request.setAttribute("hasReport", hasReport);
        getServletContext().getRequestDispatcher("/WEB-INF/jsp/event.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.debug("Event post request");
        RequestUtil.executeCommand(request, response, commandMap);
    }
}
