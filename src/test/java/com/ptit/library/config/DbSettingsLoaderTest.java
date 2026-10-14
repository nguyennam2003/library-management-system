package com.ptit.library.config;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DbSettingsLoaderTest {

    @Test
    void load_prefersEnvironmentOverFile() {
        Map<String, String> env = new HashMap<>();
        env.put("DB_HOST", "mysql");
        env.put("DB_PORT", "3306");
        env.put("DB_NAME", "library_db");
        env.put("DB_USER", "library");
        env.put("DB_PASSWORD", "from-env");
        Properties file = new Properties();
        file.setProperty("db.host", "localhost");
        file.setProperty("db.port", "3307");
        file.setProperty("db.name", "other");
        file.setProperty("db.user", "fileuser");
        file.setProperty("db.password", "from-file");

        DbSettings settings = DbSettingsLoader.load(env, file);

        assertEquals("mysql", settings.host());
        assertEquals(3306, settings.port());
        assertEquals("library_db", settings.name());
        assertEquals("library", settings.user());
        assertEquals("from-env", settings.password());
    }

    @Test
    void load_fallsBackToFileWhenEnvMissing() {
        Properties file = new Properties();
        file.setProperty("db.host", "localhost");
        file.setProperty("db.port", "3306");
        file.setProperty("db.name", "library_db");
        file.setProperty("db.user", "library");
        file.setProperty("db.password", "from-file");

        DbSettings settings = DbSettingsLoader.load(Map.of(), file);

        assertEquals("localhost", settings.host());
        assertEquals("from-file", settings.password());
    }

    @Test
    void load_throwsWhenIncomplete() {
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> DbSettingsLoader.load(Map.of(), new Properties()));
        assertTrue(ex.getMessage().contains("DB_HOST"));
    }
}
