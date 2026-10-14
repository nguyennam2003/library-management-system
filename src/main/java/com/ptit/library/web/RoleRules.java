package com.ptit.library.web;

import java.util.Set;

public final class RoleRules {

    private RoleRules() {
    }

    public static boolean permits(String path, String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        Set<String> allowed = allowedRoles(path);
        return allowed.contains(role);
    }

    static Set<String> allowedRoles(String path) {
        if (matches(path, "/admin")) {
            return Set.of("ADMIN");
        }
        if (matches(path, "/staff")) {
            return Set.of("ADMIN", "LIBRARIAN");
        }
        if (matches(path, "/reader")) {
            return Set.of("READER");
        }
        if (path.equals("/home")) {
            return Set.of("ADMIN", "LIBRARIAN", "READER");
        }
        return Set.of();
    }

    private static boolean matches(String path, String prefix) {
        return path.equals(prefix) || path.startsWith(prefix + "/");
    }
}
