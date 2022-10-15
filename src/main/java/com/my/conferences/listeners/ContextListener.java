package com.my.conferences.listeners;

import com.my.conferences.dao.factory.DaoFactory;
import com.my.conferences.dao.factory.MysqlDaoFactory;
import com.my.conferences.email.EmailManager;
import com.my.conferences.service.EventService;
import com.my.conferences.service.ReportService;
import com.my.conferences.service.UserService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebListener
public class ContextListener implements ServletContextListener, HttpSessionListener, HttpSessionAttributeListener {

    public ContextListener() {
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DaoFactory daoFactory = new MysqlDaoFactory();
        ServletContext servletContext = sce.getServletContext();

        servletContext.setAttribute("app/eventService", new EventService(daoFactory, 2));
        servletContext.setAttribute("app/reportService", new ReportService(daoFactory));
        servletContext.setAttribute("app/userService", new UserService(daoFactory));

        EmailManager.getInstance();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        EmailManager.getInstance().stopService();
    }
}
