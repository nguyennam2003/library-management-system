package com.ptit.library.config;

import java.io.IOException;
import java.io.InputStream;
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
        Properties fileProps = readOptionalClasspath("db.properties");
        return load(System.getenv(), fileProps);
    }

    static Properties readOptionalClasspath(String resourceName) {
        ClassLoader loader = DbSettingsLoader.class.getClassLoader();
        InputStream in = loader == null ? null : loader.getResourceAsStream(resourceName);
        return readOptionalStream(in, resourceName);
    }

    static Properties readOptionalStream(InputStream in, String resourceName) {
        Properties fileProps = new Properties();
        if (in == null) {
            return fileProps;
        }
        try (InputStream stream = in) {
            fileProps.load(stream);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + resourceName, e);
        }
        return fileProps;
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
