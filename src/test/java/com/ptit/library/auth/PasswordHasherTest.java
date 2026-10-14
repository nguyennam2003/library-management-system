package com.ptit.library.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void hash_isBcryptCost10_andMatchesRawPassword() {
        String hash = PasswordHasher.hash("Admin@123");
        assertTrue(hash.startsWith("$2a$10$"));
        assertTrue(PasswordHasher.check("Admin@123", hash));
        assertFalse(PasswordHasher.check("wrong-pass", hash));
    }
}
