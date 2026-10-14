package com.ptit.library.auth;

import com.ptit.library.model.AuthSession;
import com.ptit.library.model.ReaderProfile;
import com.ptit.library.model.UserAccount;

public final class CredentialVerifier {

    public Result verify(UserAccount user, ReaderProfile reader, String rawPassword) {
        if (user == null || rawPassword == null || !PasswordHasher.check(rawPassword, user.passwordHash())) {
            return Result.fail(AuthFailure.INVALID_CREDENTIALS);
        }
        if (!"READER".equals(user.roleCode()) && user.locked()) {
            return Result.fail(AuthFailure.INVALID_CREDENTIALS);
        }
        if ("READER".equals(user.roleCode())) {
            if (reader == null) {
                return Result.fail(AuthFailure.INVALID_CREDENTIALS);
            }
            if ("REJECTED".equals(reader.status())) {
                return Result.fail(AuthFailure.REJECTED);
            }
            return Result.ok(new AuthSession(
                    user.id(),
                    user.username(),
                    user.roleCode(),
                    reader.id(),
                    reader.status(),
                    reader.code()));
        }
        return Result.ok(new AuthSession(
                user.id(),
                user.username(),
                user.roleCode(),
                null,
                null,
                null));
    }

    public static final class Result {
        private final AuthSession session;
        private final AuthFailure failure;

        private Result(AuthSession session, AuthFailure failure) {
            this.session = session;
            this.failure = failure;
        }

        static Result ok(AuthSession session) {
            return new Result(session, null);
        }

        static Result fail(AuthFailure failure) {
            return new Result(null, failure);
        }

        public boolean ok() {
            return session != null;
        }

        public AuthSession session() {
            return session;
        }

        public AuthFailure failure() {
            return failure;
        }
    }
}
