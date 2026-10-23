package com.ptit.library.model;

import java.time.LocalDate;

// 1 dòng chi tiết phiếu mượn (1 cuốn sách trong 1 phiếu)
public record LoanLine(
        long detailId,
        String loanCode,
        String readerCode,
        String readerName,
        String bookTitle,
        String copyCode,
        LocalDate borrowedOn,
        LocalDate dueDate,
        LocalDate returnedAt,
        String status,
        long daysOverdue
) {
    public boolean isBorrowed() {
        return "BORROWED".equals(status);
    }
}
