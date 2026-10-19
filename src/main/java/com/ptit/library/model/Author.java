package com.ptit.library.model;

public record Author(
        long id,
        String name,
        int bookCount
) {
}
