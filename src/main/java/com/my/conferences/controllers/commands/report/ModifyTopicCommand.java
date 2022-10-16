package com.my.conferences.controllers.commands.report;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.service.DBException;
import com.my.conferences.entity.User;
import com.my.conferences.service.ReportService;
import com.my.conferences.service.ValidationException;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import java.io.IOException;

public class ModifyTopicCommand implements Command {
    private final static Logger logger = Logger.getLogger(ModifyTopicCommand.class);
    private final ReportService reportService;

    public ModifyTopicCommand(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String topic = RequestUtil.getStringParameter(request, "topic");
            logger.debug("Topic: " + topic);
            int reportId = RequestUtil.getIntParameter(request, "report_id");
            logger.debug("Report id: " + reportId);
            reportService.modifyReportTopic(reportId, topic, (User) request.getSession().getAttribute("user"));
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
