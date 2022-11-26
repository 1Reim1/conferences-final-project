package com.my.conferences.controllers.commands.report;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.exception.DBException;
import com.my.conferences.service.ReportService;
import com.my.conferences.exception.ValidationException;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import java.io.IOException;

public class ConfirmCommand implements Command {

    private final static String EXCEPTION_MESSAGE = "Exception in ConfirmCommand";
    private final static Logger logger = Logger.getLogger(ConfirmCommand.class);
    private final ReportService reportService;

    public ConfirmCommand(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int reportId = RequestUtil.getIntParameter(request, "report_id");
            logger.trace("Report id: " + reportId);
            reportService.confirmReport(reportId, RequestUtil.getUser(request));
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
