package com.ptit.library.web;

import com.ptit.library.model.AuthSession;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleGuardTest {

    private static AuthSession session(String role) {
        Long readerId = "READER".equals(role) ? 1L : null;
        return new AuthSession(1L, "user", role, readerId, "ACTIVE", "DG000001");
    }

    @Test
    void requireRole_allowsMatchingRole() {
        assertTrue(RoleGuard.hasRole(session("ADMIN"), "ADMIN"));
        assertTrue(RoleGuard.hasRole(session("LIBRARIAN"), "ADMIN", "LIBRARIAN"));
        assertTrue(RoleGuard.hasRole(session("READER"), "READER"));
    }

    @Test
    void requireRole_rejectsOtherRolesAndNull() {
        assertFalse(RoleGuard.hasRole(session("READER"), "ADMIN", "LIBRARIAN"));
        assertFalse(RoleGuard.hasRole(session("LIBRARIAN"), "ADMIN"));
        assertFalse(RoleGuard.hasRole(null, "ADMIN"));
    }
}
