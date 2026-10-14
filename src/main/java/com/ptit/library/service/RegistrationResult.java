package com.ptit.library.service;

import java.util.List;

public final class RegistrationResult {

    private final boolean ok;
    private final String readerCode;
    private final List<String> errors;

    private RegistrationResult(boolean ok, String readerCode, List<String> errors) {
        this.ok = ok;
        this.readerCode = readerCode;
        this.errors = errors;
    }

    public static RegistrationResult ok(String readerCode) {
        return new RegistrationResult(true, readerCode, List.of());
    }

    public static RegistrationResult invalid(List<String> errors) {
        return new RegistrationResult(false, null, List.copyOf(errors));
    }

    public boolean ok() {
        return ok;
    }

    public String readerCode() {
        return readerCode;
    }

    public List<String> errors() {
        return errors;
    }
}
