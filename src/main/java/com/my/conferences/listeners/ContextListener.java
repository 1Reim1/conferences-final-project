package com.my.conferences.listeners;

import com.my.conferences.dao.EventDao;
import com.my.conferences.dao.ReportDao;
import com.my.conferences.dao.UserDao;
import com.my.conferences.dao.VerificationCodeDao;
import com.my.conferences.dao.factory.DaoFactory;
import com.my.conferences.dao.factory.MysqlDaoFactory;
import com.my.conferences.email.EmailManager;
import com.my.conferences.service.*;
import com.my.conferences.util.PropertiesUtil;
import com.my.conferences.validation.RecaptchaValidation;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionListener;
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
        int usersPageSize;
        RecaptchaValidation recaptchaValidation;
        try {
            Properties appConfig = PropertiesUtil.loadFromResources("app.properties");
            homePageSize = Integer.parseInt(appConfig.getProperty("home.page.size"));
            usersPageSize = Integer.parseInt(appConfig.getProperty("users.page.size"));
            String siteKey = appConfig.getProperty("recaptcha.site_key");
            String secretKey = appConfig.getProperty("recaptcha.secret_key");
            recaptchaValidation = new RecaptchaValidation(siteKey, secretKey);

            logger.debug("home.page.size = " + homePageSize);
            logger.debug("users.page.size = " + usersPageSize);
            logger.debug("recaptcha.site_key = " + siteKey);

            Properties emailConfig = PropertiesUtil.loadFromResources("email.properties");
            emailManager = new EmailManager(emailConfig);
        } catch (IOException | NumberFormatException e) {
            logger.error("Loading app config exception", e);
            throw new RuntimeException(e);
        }

        ConnectionManager connectionManager = new ConnectionManager();
        DaoFactory daoFactory = new MysqlDaoFactory();
        EventDao eventDao = daoFactory.getEventDao();
        ReportDao reportDao = daoFactory.getReportDao();
        UserDao userDao = daoFactory.getUserDao();
        VerificationCodeDao verificationCodeDao = daoFactory.getVerificationCodeDao();

        ServletContext servletContext = sce.getServletContext();
        servletContext.setAttribute("app/eventService", new EventService(emailManager, connectionManager, eventDao, reportDao, userDao, homePageSize));
        servletContext.setAttribute("app/reportService", new ReportService(emailManager, connectionManager, eventDao, reportDao, userDao));
        servletContext.setAttribute("app/userService", new UserService(emailManager, connectionManager, userDao, reportDao, eventDao, verificationCodeDao, usersPageSize));
        servletContext.setAttribute("app/verificationCodeService", new VerificationCodeService(emailManager, connectionManager, verificationCodeDao, userDao));
        servletContext.setAttribute("app/recaptchaValidation", recaptchaValidation);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Stopping");
        emailManager.stopService();
    }
}
