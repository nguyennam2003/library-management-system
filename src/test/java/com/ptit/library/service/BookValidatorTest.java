package com.ptit.library.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookValidatorTest {

    private final BookValidator validator = new BookValidator();

    @Test
    void normalizesOptionalAndHyphenatedIsbn() {
        assertNull(BookValidator.normalizeIsbn(null));
        assertNull(BookValidator.normalizeIsbn("   "));
        assertEquals("9780132350884", BookValidator.normalizeIsbn("978-0-13-235088-4"));
        assertEquals("020163361X", BookValidator.normalizeIsbn("0-201-63361-x"));
    }

    @Test
    void acceptsValidBookWithOptionalOrValidIsbn() {
        List<String> noIsbnErrors = validator.validateBook(
                "Cho tôi xin một vé đi tuổi thơ", "", 2L, List.of(1L), "NXB Trẻ", 2008);
        assertTrue(noIsbnErrors.isEmpty());

        List<String> isbn13Errors = validator.validateBook(
                "Clean Code", "978-0132350884", 1L, List.of(2L, 3L), "Prentice Hall", 2008);
        assertTrue(isbn13Errors.isEmpty());
    }

    @Test
    void rejectsMissingRequiredFieldsOrInvalidIsbn() {
        List<String> errors = validator.validateBook(
                " ", "12345", null, List.of(), "", 1200);
        assertFalse(errors.isEmpty());
        assertEquals(6, errors.size());
    }

    @Test
    void validatesPhysicalCopyCode() {
        assertNull(validator.validateCopyCode("CC-001"));
        assertTrue(validator.validateCopyCode("  ").contains("trống"));
    }
}
