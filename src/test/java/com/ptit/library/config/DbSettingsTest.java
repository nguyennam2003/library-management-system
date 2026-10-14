package com.ptit.library.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DbSettingsTest {

    @Test
    void jdbcUrl_usesUtf8AndVietnamTimezone() {
        DbSettings settings = new DbSettings("mysql", 3306, "library_db", "library", "secret");
        String url = settings.jdbcUrl();
        assertTrue(url.startsWith("jdbc:mysql://mysql:3306/library_db?"));
        assertTrue(url.contains("characterEncoding=UTF-8"));
        assertTrue(url.contains("connectionCollation=utf8mb4_unicode_ci"));
        assertTrue(url.contains("serverTimezone=Asia/Ho_Chi_Minh"));
        assertTrue(url.contains("allowPublicKeyRetrieval=true"));
        assertTrue(url.contains("useSSL=false"));
    }
}
