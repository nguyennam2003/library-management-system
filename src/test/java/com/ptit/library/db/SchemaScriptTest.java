package com.ptit.library.db;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaScriptTest {

    private static String sql;

    @BeforeAll
    static void load() throws IOException {
        sql = Files.readString(Path.of("db/01_schema.sql"), StandardCharsets.UTF_8);
    }

    @Test
    void createsEverySpecifiedTableAndOmitsFines() {
        assertTrue(sql.contains("CREATE TABLE roles"));
        assertTrue(sql.contains("CREATE TABLE users"));
        assertTrue(sql.contains("CREATE TABLE readers"));
        assertTrue(sql.contains("CREATE TABLE categories"));
        assertTrue(sql.contains("CREATE TABLE authors"));
        assertTrue(sql.contains("CREATE TABLE books"));
        assertTrue(sql.contains("CREATE TABLE book_authors"));
        assertTrue(sql.contains("CREATE TABLE book_copies"));
        assertTrue(sql.contains("CREATE TABLE loans"));
        assertTrue(sql.contains("CREATE TABLE loan_details"));
        assertTrue(sql.contains("CREATE TABLE system_settings"));
        assertFalse(sql.contains("CREATE TABLE fines"));
    }

    @Test
    void usesUtf8mb4AndRequiredStatuses() {
        assertTrue(sql.contains("utf8mb4"));
        assertTrue(sql.contains("PENDING"));
        assertTrue(sql.contains("AVAILABLE"));
        assertTrue(sql.contains("borrowed_copy_key"));
        assertTrue(sql.contains("max_borrow_count"));
        assertTrue(sql.contains("loan_period_days"));
    }
}
