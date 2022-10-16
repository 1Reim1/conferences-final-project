package com.my.conferences.controllers.commands.report;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.service.DBException;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;
import com.my.conferences.service.ReportService;
import com.my.conferences.service.ValidationException;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import java.io.IOException;

public class OfferCommand implements Command {

    private final static Logger logger = Logger.getLogger(OfferCommand.class);
    private final ReportService reportService;

    public OfferCommand(ReportService reportService) {
        this.reportService = reportService;
    }

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
            logger.debug("Topic: " + report.getTopic());
            logger.debug("Event id: " + report.getEventId());
            logger.debug("Creator id: " + report.getCreator().getId());
            logger.debug("Speaker id: " + speaker.getId());
            reportService.offerReport(report);
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
