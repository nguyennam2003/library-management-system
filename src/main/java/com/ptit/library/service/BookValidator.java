package com.ptit.library.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class BookValidator {

    private static final Pattern ISBN_10 = Pattern.compile("^\\d{9}[\\dX]$");
    private static final Pattern ISBN_13 = Pattern.compile("^\\d{13}$");

    public static String normalizeIsbn(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.trim().replace("-", "").replace(" ", "").toUpperCase();
        return cleaned.isEmpty() ? null : cleaned;
    }

    public static boolean isValidIsbn(String normalized) {
        if (normalized == null || normalized.isEmpty()) {
            return true;
        }
        return ISBN_10.matcher(normalized).matches() || ISBN_13.matcher(normalized).matches();
    }

    public List<String> validateBook(
            String title,
            String rawIsbn,
            Long categoryId,
            List<Long> authorIds,
            String publisher,
            Integer publishYear) {
        List<String> errors = new ArrayList<>();
        String cleanTitle = title == null ? "" : title.trim();
        String cleanPublisher = publisher == null ? "" : publisher.trim();
        String normalizedIsbn = normalizeIsbn(rawIsbn);

        if (cleanTitle.isEmpty()) {
            errors.add("Tên đầu sách không được để trống.");
        } else if (cleanTitle.length() > 255) {
            errors.add("Tên đầu sách không được vượt quá 255 ký tự.");
        }

        if (!isValidIsbn(normalizedIsbn)) {
            errors.add("ISBN không hợp lệ (chỉ chấp nhận ISBN-10 hoặc ISBN-13, có thể kèm dấu gạch ngang).");
        }

        if (categoryId == null || categoryId <= 0) {
            errors.add("Vui lòng chọn một thể loại cho đầu sách.");
        }

        if (authorIds == null || authorIds.isEmpty()) {
            errors.add("Vui lòng chọn ít nhất một tác giả.");
        }

        if (cleanPublisher.isEmpty()) {
            errors.add("Nhà xuất bản không được để trống.");
        } else if (cleanPublisher.length() > 128) {
            errors.add("Tên nhà xuất bản không được vượt quá 128 ký tự.");
        }

        if (publishYear == null || publishYear < 1500 || publishYear > 2100) {
            errors.add("Năm xuất bản không hợp lệ.");
        }

        return errors;
    }

    public String validateCopyCode(String rawCode) {
        String code = rawCode == null ? "" : rawCode.trim();
        if (code.isEmpty()) {
            return "Mã bản sách không được để trống.";
        }
        if (code.length() > 32) {
            return "Mã bản sách không được vượt quá 32 ký tự.";
        }
        return null;
    }
}
