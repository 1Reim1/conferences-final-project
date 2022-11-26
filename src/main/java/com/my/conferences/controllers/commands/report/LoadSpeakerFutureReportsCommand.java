package com.my.conferences.controllers.commands.report;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.dto.ReportWithEvent;
import com.my.conferences.exception.DBException;
import com.my.conferences.service.ReportService;
import com.my.conferences.exception.ValidationException;
import com.my.conferences.util.JsonUtil;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class LoadSpeakerFutureReportsCommand implements Command {

    private final static String EXCEPTION_MESSAGE = "Exception in LoadSpeakerFutureReportsCommand";
    private final static Logger logger = Logger.getLogger(LoadSpeakerFutureReportsCommand.class);
    private final ReportService reportService;

    public LoadSpeakerFutureReportsCommand(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int speakerId = RequestUtil.getIntParameter(request, "speaker_id");
            logger.trace("Speaker id: " + speakerId);
            List<ReportWithEvent> reportsWithEvents = reportService.findAllFutureSpeakerReports(speakerId);
            response.getWriter().println(JsonUtil.reportsWithEventsToJson(reportsWithEvents));
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
