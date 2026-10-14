package com.ptit.library.model;

public record UserAccount(
        long id,
        String username,
        String passwordHash,
        long roleId,
        String roleCode,
        boolean locked
) {
}
