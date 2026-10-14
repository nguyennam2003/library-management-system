package com.ptit.library.web;

import com.ptit.library.config.DbSettings;
import com.ptit.library.config.DbSettingsLoader;
import com.ptit.library.dao.ConnectionFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppContextListener implements ServletContextListener {

    public static final String CONNECTION_FACTORY = "connectionFactory";
    private static final Logger LOGGER = Logger.getLogger(AppContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            DbSettings settings = DbSettingsLoader.loadFromEnvironment();
            event.getServletContext().setAttribute(CONNECTION_FACTORY, new ConnectionFactory(settings));
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Database settings missing; /health will return 503", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        event.getServletContext().removeAttribute(CONNECTION_FACTORY);
    }
}
