package com.my.conferences.controllers;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.controllers.commands.event.*;
import com.my.conferences.controllers.commands.report.CancelCommand;
import com.my.conferences.controllers.commands.report.ConfirmCommand;
import com.my.conferences.controllers.commands.report.ModifyTopicCommand;
import com.my.conferences.controllers.commands.report.OfferCommand;
import com.my.conferences.controllers.commands.user.SearchAvailableSpeakersCommand;
import com.my.conferences.db.DBException;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;
import com.my.conferences.logic.EventManager;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

@WebServlet(value = "/event")
public class EventServlet extends HttpServlet {
    private static final HashMap<String, Command> commandMap = new HashMap<>();
    private static final EventManager eventManager = EventManager.getInstance();

    @Override
    public void init() {
        commandMap.put("join", new JoinCommand());
        commandMap.put("leave", new LeaveCommand());
        commandMap.put("hide", new HideCommand());
        commandMap.put("show", new ShowCommand());
        commandMap.put("modify-title", new ModifyTitleCommand());
        commandMap.put("modify-description", new ModifyDescriptionCommand());
        commandMap.put("modify-date", new ModifyDateCommand());
        commandMap.put("modify-place", new ModifyPlaceCommand());
        commandMap.put("modify-statistics", new ModifyStatisticsCommand());
        commandMap.put("cancel-report", new CancelCommand());
        commandMap.put("confirm-report", new ConfirmCommand());
        commandMap.put("offer-report", new OfferCommand());
        commandMap.put("modify-report-topic", new ModifyTopicCommand());
        commandMap.put("search-speaker", new SearchAvailableSpeakersCommand());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int id;
        try {
            id = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Expected 'id' should be integer");
            return;
        }

        Event event;
        try {
            event = eventManager.findOne(id, ((User) request.getSession().getAttribute("user")).getRole() != User.Role.USER);
            request.setAttribute("event", event);
        } catch (DBException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println(e.getMessage());
            return;
        }

        User user = (User) request.getSession().getAttribute("user");
        boolean isParticipant = false;
        if (!user.equals(event.getModerator())) {
            for (User participant : event.getParticipants())
                if (participant.equals(user)) {
                    isParticipant = true;
                    break;
                }
            request.setAttribute("isParticipant", isParticipant);
        }
        if (user.getRole() == User.Role.SPEAKER) {
            boolean hasReport = false;
            for (Report report : event.getReports())
                if (report.getSpeaker().equals(user)) {
                    hasReport = true;
                    break;
                }
            request.setAttribute("hasReport", hasReport);
        }

        request.setAttribute("isModerator", event.getModerator().equals(user));
        request.setAttribute("isFutureEvent", event.getDate().compareTo(new Date()) > 0);
        getServletContext().getRequestDispatcher("/WEB-INF/jsp/event.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String commandKey = request.getParameter("command");
        Command command = commandMap.get(commandKey);
        if (command == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().printf("Command '%s' is unknown.", commandKey);
            return;
        }

        command.execute(request, response);
    }
}
