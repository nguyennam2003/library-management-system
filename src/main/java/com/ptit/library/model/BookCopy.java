package com.ptit.library.model;

public record BookCopy(
        long id,
        long bookId,
        String code,
        String status,
        boolean hasLoanHistory
) {
    public boolean isAvailable() {
        return "AVAILABLE".equals(status);
    }

    public boolean isBorrowed() {
        return "BORROWED".equals(status);
    }

    public boolean isWithdrawn() {
        return "WITHDRAWN".equals(status);
    }
}
