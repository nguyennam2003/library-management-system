package com.ptit.library.model;

public record AuthSession(
        long userId,
        String username,
        String role,
        Long readerId,
        String readerStatus,
        String readerCode
) {
    public boolean isStaff() {
        return "ADMIN".equals(role) || "LIBRARIAN".equals(role);
    }

    public boolean isReader() {
        return "READER".equals(role);
    }
}
