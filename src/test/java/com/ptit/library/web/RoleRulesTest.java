package com.ptit.library.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleRulesTest {

    @Test
    void adminPrefix_isAdminOnly() {
        assertTrue(RoleRules.permits("/admin", "ADMIN"));
        assertTrue(RoleRules.permits("/admin/settings", "ADMIN"));
        assertFalse(RoleRules.permits("/admin/settings", "LIBRARIAN"));
        assertFalse(RoleRules.permits("/admin/settings", "READER"));
    }

    @Test
    void staffPrefix_isAdminAndLibrarian() {
        assertTrue(RoleRules.permits("/staff/home", "ADMIN"));
        assertTrue(RoleRules.permits("/staff/home", "LIBRARIAN"));
        assertFalse(RoleRules.permits("/staff/home", "READER"));
    }

    @Test
    void readerPrefix_isReaderOnly() {
        assertTrue(RoleRules.permits("/reader/home", "READER"));
        assertFalse(RoleRules.permits("/reader/home", "ADMIN"));
        assertFalse(RoleRules.permits("/reader/home", "LIBRARIAN"));
    }

    @Test
    void home_anyAuthenticatedRole() {
        assertTrue(RoleRules.permits("/home", "ADMIN"));
        assertTrue(RoleRules.permits("/home", "LIBRARIAN"));
        assertTrue(RoleRules.permits("/home", "READER"));
        assertTrue(RoleRules.permits("/account/password", "ADMIN"));
        assertTrue(RoleRules.permits("/account/password", "LIBRARIAN"));
        assertTrue(RoleRules.permits("/account/password", "READER"));
    }

    @Test
    void unknownPath_isDenied() {
        assertFalse(RoleRules.permits("/secret", "ADMIN"));
        assertFalse(RoleRules.permits("/home", null));
    }
}
