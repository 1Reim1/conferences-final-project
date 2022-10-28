package com.my.conferences.controllers;

import com.my.conferences.service.DBException;
import com.my.conferences.service.ReportService;
import com.my.conferences.service.ValidationException;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import java.io.IOException;

@WebServlet(value = "/new-reports")
public class NewReportsServlet extends HttpServlet {

    private final static String EXCEPTION_MESSAGE = "Exception in NewReportsServlet";
    private final static Logger logger = Logger.getLogger(NewReportsServlet.class);
    private ReportService reportService;

    @Override
    public void init() {
        reportService = (ReportService) getServletContext().getAttribute("app/reportService");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setAttribute("reportWithEventList", reportService.findNewReports(RequestUtil.getUser(request)));
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

        getServletContext().getRequestDispatcher("/WEB-INF/jsp/new-reports.jsp").forward(request, response);
    }
}
