package com.my.conferences.controllers;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.controllers.commands.event.JoinCommand;
import com.my.conferences.controllers.commands.event.LeaveCommand;
import com.my.conferences.controllers.commands.report.CancelCommand;
import com.my.conferences.controllers.commands.report.ConfirmCommand;
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
import java.util.HashMap;

@WebServlet(value = "/event")
public class EventServlet extends HttpServlet {
    private static final HashMap<String, Command> commandMap = new HashMap<>();
    private static final EventManager eventManager = EventManager.getInstance();

    @Override
    public void init() {
        commandMap.put("join", new JoinCommand());
        commandMap.put("leave", new LeaveCommand());
        commandMap.put("cancel-report", new CancelCommand());
        commandMap.put("confirm-report", new ConfirmCommand());
        commandMap.put("offer-report", new OfferCommand());
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

        getServletContext().getRequestDispatcher("/event.jsp").forward(request, response);
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
