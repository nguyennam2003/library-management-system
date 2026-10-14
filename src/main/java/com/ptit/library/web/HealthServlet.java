package com.ptit.library.web;

import com.ptit.library.dao.ConnectionFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain; charset=UTF-8");
        ConnectionFactory factory =
                (ConnectionFactory) getServletContext().getAttribute(AppContextListener.CONNECTION_FACTORY);
        if (factory == null) {
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            resp.getWriter().write("DB_ERROR");
            return;
        }
        try (Connection connection = factory.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT 1")) {
            if (rs.next()) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("OK");
                return;
            }
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            resp.getWriter().write("DB_ERROR");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            resp.getWriter().write("DB_ERROR");
        }
    }
}
