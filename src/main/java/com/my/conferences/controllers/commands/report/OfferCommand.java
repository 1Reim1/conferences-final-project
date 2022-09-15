package com.my.conferences.controllers.commands.report;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.db.DBException;
import com.my.conferences.entity.User;
import com.my.conferences.logic.ReportManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class OfferCommand implements Command {
    private static final ReportManager reportManager = ReportManager.getInstance();
    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String topic = request.getParameter("topic");

        int speakerId;
        try {
            speakerId = Integer.parseInt(request.getParameter("speakerId"));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Expected 'speakerId' should be integer");
            return;
        }

        int eventId;
        try {
            eventId = Integer.parseInt(request.getParameter("eventId"));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Expected 'eventId' should be integer");
            return;
        }

        try {
            reportManager.offerReport(eventId, topic, speakerId, (User) request.getSession().getAttribute("user"));
        } catch (DBException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
        }
    }
}
