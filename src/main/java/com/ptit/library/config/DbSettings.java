package com.ptit.library.config;

import java.util.Objects;

public final class DbSettings {

    private final String host;
    private final int port;
    private final String name;
    private final String user;
    private final String password;

    public DbSettings(String host, int port, String name, String user, String password) {
        this.host = Objects.requireNonNull(host, "host");
        this.port = port;
        this.name = Objects.requireNonNull(name, "name");
        this.user = Objects.requireNonNull(user, "user");
        this.password = Objects.requireNonNull(password, "password");
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("invalid db port: " + port);
        }
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String name() {
        return name;
    }

    public String user() {
        return user;
    }

    public String password() {
        return password;
    }

    public String jdbcUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + name
                + "?useSSL=false"
                + "&allowPublicKeyRetrieval=true"
                + "&characterEncoding=UTF-8"
                + "&connectionCollation=utf8mb4_unicode_ci"
                + "&serverTimezone=Asia/Ho_Chi_Minh";
    }
}
