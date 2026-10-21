package com.ptit.library.web;

import com.ptit.library.config.DbSettings;
import com.ptit.library.config.DbSettingsLoader;
import com.ptit.library.dao.ConnectionFactory;
import com.ptit.library.service.AuthService;
import com.ptit.library.service.BookCatalogService;
import com.ptit.library.service.DashboardService;
import com.ptit.library.service.LoanService;
import com.ptit.library.service.ReaderSelfService;
import com.ptit.library.service.RegistrationService;
import com.ptit.library.service.SettingService;
import com.ptit.library.service.StaffReaderService;
import com.ptit.library.service.StaffUserService;

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
            ConnectionFactory factory = new ConnectionFactory(settings);
            event.getServletContext().setAttribute(CONNECTION_FACTORY, factory);
            event.getServletContext().setAttribute(LoginServlet.AUTH_SERVICE, new AuthService(factory));
            event.getServletContext().setAttribute(RegisterServlet.REGISTRATION_SERVICE, new RegistrationService(factory));
            event.getServletContext().setAttribute(BookCatalogService.ATTR_NAME, new BookCatalogService(factory));
            event.getServletContext().setAttribute(ReaderSelfService.ATTR_NAME, new ReaderSelfService(factory));
            event.getServletContext().setAttribute(StaffReaderService.ATTR_NAME, new StaffReaderService(factory));
            event.getServletContext().setAttribute(LoanService.ATTR_NAME, new LoanService(factory));
            event.getServletContext().setAttribute(DashboardService.ATTR_NAME, new DashboardService(factory));
            event.getServletContext().setAttribute(StaffUserService.ATTR_NAME, new StaffUserService(factory));
            event.getServletContext().setAttribute(SettingService.ATTR_NAME, new SettingService(factory));
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Database settings missing; /health will return 503", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        event.getServletContext().removeAttribute(CONNECTION_FACTORY);
    }
}
