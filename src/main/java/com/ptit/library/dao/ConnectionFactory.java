package com.ptit.library.dao;

import com.ptit.library.config.DbSettings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public final class ConnectionFactory {

    private final DbSettings settings;

    public ConnectionFactory(DbSettings settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL JDBC driver not found", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(settings.jdbcUrl(), settings.user(), settings.password());
    }
}
