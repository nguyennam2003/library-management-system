package com.ptit.library.model;

import java.time.LocalDate;

public record ReaderLoanItem(
        long detailId,
        long loanId,
        String loanCode,
        String loanStatus,
        LocalDate borrowedOn,
        long bookId,
        String bookTitle,
        String copyCode,
        LocalDate dueDate,
        LocalDate returnedAt,
        String detailStatus,
        boolean overdue
) {
    public boolean isBorrowed() {
        return "BORROWED".equals(detailStatus);
    }
}
