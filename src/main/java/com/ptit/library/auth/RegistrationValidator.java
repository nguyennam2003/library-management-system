package com.ptit.library.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class RegistrationValidator {

    private static final Pattern USERNAME = Pattern.compile("^[a-zA-Z0-9._]{4,32}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^0?\\d{9,14}$");

    public List<String> validate(RegistrationInput input) {
        List<String> errors = new ArrayList<>();
        String username = trim(input.username());
        String password = input.password() == null ? "" : input.password();
        String confirm = input.confirmPassword() == null ? "" : input.confirmPassword();
        String fullName = trim(input.fullName());
        String email = trim(input.email());
        String phone = trim(input.phone()).replaceAll("\\s+", "");

        if (!USERNAME.matcher(username).matches()) {
            errors.add("Tên đăng nhập phải từ 4–32 ký tự, gồm chữ, số, dấu chấm hoặc gạch dưới.");
        }
        if (password.length() < 8) {
            errors.add("Mật khẩu phải có ít nhất 8 ký tự.");
        }
        if (!password.equals(confirm)) {
            errors.add("Xác nhận mật khẩu không khớp.");
        }
        if (fullName.isBlank()) {
            errors.add("Họ tên không được để trống.");
        }
        if (!EMAIL.matcher(email).matches()) {
            errors.add("Email không hợp lệ.");
        }
        if (!PHONE.matcher(phone).matches()) {
            errors.add("Số điện thoại không hợp lệ.");
        }
        return errors;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
