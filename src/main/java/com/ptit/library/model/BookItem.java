package com.ptit.library.model;

import java.util.List;

public record BookItem(
        long id,
        String title,
        String isbn,
        long categoryId,
        String categoryName,
        String publisher,
        int publishYear,
        String status,
        List<Long> authorIds,
        String authorNames,
        int totalCopies,
        int availableCopies,
        boolean hasLoanHistory
) {
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}
