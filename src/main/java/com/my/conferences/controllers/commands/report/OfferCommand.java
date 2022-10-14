package com.my.conferences.controllers.commands.report;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.db.DBException;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;
import com.my.conferences.logic.ReportManager;
import com.my.conferences.logic.ValidationException;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class OfferCommand implements Command {
    private static final ReportManager reportManager = ReportManager.getInstance();

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Report report = new Report();
            report.setTopic(RequestUtil.getStringParameter(request, "topic"));
            report.setEventId(RequestUtil.getIntParameter(request, "event_id"));
            report.setCreator((User) request.getSession().getAttribute("user"));
            User speaker = new User();
            speaker.setId(RequestUtil.getIntParameter(request, "speaker_id"));
            report.setSpeaker(speaker);
            reportManager.offerReport(report);
        } catch (ValidationException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
        } catch (DBException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
        }
    }
}
