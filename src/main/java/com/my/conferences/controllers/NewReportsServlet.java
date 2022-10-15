package com.my.conferences.controllers;

import com.my.conferences.db.DBException;
import com.my.conferences.entity.User;
import com.my.conferences.logic.ReportManager;
import com.my.conferences.logic.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(value = "/new-reports")
public class NewReportsServlet extends HttpServlet {
    private static final ReportManager reportManager = ReportManager.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setAttribute("reportWithEventList", reportManager.findNewReports((User) request.getSession().getAttribute("user")));
        } catch (ValidationException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
            return;
        } catch (DBException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
            return;
        }

        getServletContext().getRequestDispatcher("/WEB-INF/jsp/new-reports.jsp").forward(request, response);
    }
}
