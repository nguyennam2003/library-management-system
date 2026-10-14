package com.ptit.library.config;

import java.util.Map;
import java.util.Properties;

public final class DbSettingsLoader {

    private DbSettingsLoader() {
    }

    public static DbSettings load(Map<String, String> env, Properties fileProps) {
        String host = first(env.get("DB_HOST"), fileProps.getProperty("db.host"), "DB_HOST / db.host");
        String portRaw = first(env.get("DB_PORT"), fileProps.getProperty("db.port"), "DB_PORT / db.port");
        String name = first(env.get("DB_NAME"), fileProps.getProperty("db.name"), "DB_NAME / db.name");
        String user = first(env.get("DB_USER"), fileProps.getProperty("db.user"), "DB_USER / db.user");
        String password = first(env.get("DB_PASSWORD"), fileProps.getProperty("db.password"), "DB_PASSWORD / db.password");
        int port;
        try {
            port = Integer.parseInt(portRaw);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("DB_PORT / db.port must be an integer", e);
        }
        return new DbSettings(host, port, name, user, password);
    }

    public static DbSettings loadFromEnvironment() {
        return load(System.getenv(), new Properties());
    }

    private static String first(String envValue, String fileValue, String label) {
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        if (fileValue != null && !fileValue.isBlank()) {
            return fileValue;
        }
        throw new IllegalStateException("Missing database setting: " + label);
    }
}
