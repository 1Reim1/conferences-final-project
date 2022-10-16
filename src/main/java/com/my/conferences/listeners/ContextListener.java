package com.my.conferences.listeners;

import com.my.conferences.dao.EventDao;
import com.my.conferences.dao.ReportDao;
import com.my.conferences.dao.UserDao;
import com.my.conferences.dao.factory.DaoFactory;
import com.my.conferences.dao.factory.MysqlDaoFactory;
import com.my.conferences.email.EmailManager;
import com.my.conferences.service.EventService;
import com.my.conferences.service.ReportService;
import com.my.conferences.service.UserService;
import com.my.conferences.util.PropertiesUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

@WebListener
public class ContextListener implements ServletContextListener, HttpSessionListener, HttpSessionAttributeListener {
    private final static Logger logger = Logger.getLogger(ContextListener.class);
    private EmailManager emailManager;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Starting");
        int homePageSize;
        try {
            Properties appConfig = PropertiesUtil.loadFromResources("app.properties");
            homePageSize = Integer.parseInt(appConfig.getProperty("home.page.size"));
            logger.debug("home.page.size = " + homePageSize);

            Properties emailConfig = PropertiesUtil.loadFromResources("email.properties");
            emailManager = new EmailManager(emailConfig);
        } catch (IOException | NumberFormatException e) {
            logger.error("Loading app config exception", e);
            throw new RuntimeException(e);
        }

        DaoFactory daoFactory = new MysqlDaoFactory();
        EventDao eventDao = daoFactory.getEventDao();
        ReportDao reportDao = daoFactory.getReportDao();
        UserDao userDao = daoFactory.getUserDao();

        ServletContext servletContext = sce.getServletContext();
        servletContext.setAttribute("app/eventService", new EventService(emailManager, eventDao, reportDao, userDao, homePageSize));
        servletContext.setAttribute("app/reportService", new ReportService(emailManager, eventDao, reportDao, userDao));
        servletContext.setAttribute("app/userService", new UserService(userDao));
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Stopping");
        emailManager.stopService();
    }
}
