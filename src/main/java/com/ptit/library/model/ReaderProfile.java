package com.ptit.library.model;

public record ReaderProfile(
        long id,
        long userId,
        String code,
        String fullName,
        String email,
        String phone,
        String status
) {
}
