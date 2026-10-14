package com.ptit.library.auth;

public record RegistrationInput(
        String username,
        String password,
        String confirmPassword,
        String fullName,
        String email,
        String phone
) {
}
