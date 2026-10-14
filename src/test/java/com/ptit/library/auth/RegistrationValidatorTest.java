package com.ptit.library.auth;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrationValidatorTest {

    private final RegistrationValidator validator = new RegistrationValidator();

    @Test
    void validInput_hasNoErrors() {
        List<String> errors = validator.validate(valid());
        assertTrue(errors.isEmpty());
    }

    @Test
    void rejectsShortUsernameAndPasswordMismatch() {
        RegistrationInput input = new RegistrationInput(
                "ab", "short", "other", "Nguyen Van A", "not-an-email", "");
        List<String> errors = validator.validate(input);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("đăng nhập") || e.toLowerCase().contains("username")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("mật khẩu")));
        assertTrue(errors.stream().anyMatch(e -> e.toLowerCase().contains("email")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("điện thoại") || e.toLowerCase().contains("số")));
    }

    @Test
    void usernameAllowsDotUnderscore() {
        RegistrationInput input = new RegistrationInput(
                "ban.doc_01", "Reader@123", "Reader@123", "Nguyen Van A", "a@b.co", "0901234567");
        assertTrue(validator.validate(input).isEmpty());
    }

    private static RegistrationInput valid() {
        return new RegistrationInput(
                "bandoc01", "Reader@123", "Reader@123", "Nguyễn Văn A", "a@example.com", "0901234567");
    }
}
