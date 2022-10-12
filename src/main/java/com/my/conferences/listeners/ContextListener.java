package com.my.conferences.listeners;

import com.my.conferences.email.EmailManager;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebListener
public class ContextListener implements ServletContextListener, HttpSessionListener, HttpSessionAttributeListener {

    public ContextListener() {
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        EmailManager.getInstance();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        EmailManager.getInstance().stopService();
    }
}
