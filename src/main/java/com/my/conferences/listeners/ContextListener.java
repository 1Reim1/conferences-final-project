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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@WebListener
public class ContextListener implements ServletContextListener, HttpSessionListener, HttpSessionAttributeListener {

    public ContextListener() {
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        int homePageSize;
        try (InputStream inputStream = loader.getResourceAsStream("app.properties")) {
            Properties config = new Properties();
            config.load(inputStream);
            homePageSize = Integer.parseInt(config.getProperty("home.page.size"));
        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Loading app config exception", e);
        }

        DaoFactory daoFactory = new MysqlDaoFactory();
        ServletContext servletContext = sce.getServletContext();

        servletContext.setAttribute("app/eventService", new EventService(daoFactory, homePageSize));
        servletContext.setAttribute("app/reportService", new ReportService(daoFactory));
        servletContext.setAttribute("app/userService", new UserService(daoFactory));

        EmailManager.getInstance();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        EmailManager.getInstance().stopService();
    }
}
