package com.ptit.library.db;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SeedScriptTest {

    private static String sql;

    @BeforeAll
    static void load() throws IOException {
        sql = Files.readString(Path.of("db/02_seed.sql"), StandardCharsets.UTF_8);
    }

    @Test
    void seedsRequiredDemoRows() {
        assertTrue(sql.contains("'ADMIN'"));
        assertTrue(sql.contains("'LIBRARIAN'"));
        assertTrue(sql.contains("'READER'"));
        assertTrue(sql.contains("'admin'"));
        assertTrue(sql.contains("'librarian'"));
        assertTrue(sql.contains("'reader1'"));
        assertTrue(sql.contains("'reader2'"));
        assertTrue(sql.contains("'DG000001'"));
        assertTrue(sql.contains("'DG000002'"));
        assertTrue(sql.contains("'PENDING'"));
        assertTrue(sql.contains("'ACTIVE'"));
        assertTrue(sql.contains("'max_borrow_count'"));
        assertTrue(sql.contains("'loan_period_days'"));
        assertFalseContainsFines();
    }

    @Test
    void adminHashMatchesKnownPassword() {
        Matcher matcher = Pattern.compile("\\$2a\\$10\\$[A-Za-z0-9./]{53}").matcher(sql);
        assertTrue(matcher.find(), "expected bcrypt hashes in seed");
        boolean adminOk = false;
        matcher.reset();
        while (matcher.find()) {
            if (BCrypt.checkpw("Admin@123", matcher.group())) {
                adminOk = true;
            }
        }
        assertTrue(adminOk, "seed must contain BCrypt hash for Admin@123");
    }

    private static void assertFalseContainsFines() {
        org.junit.jupiter.api.Assertions.assertFalse(sql.toLowerCase().contains("insert into fines"));
    }
}
