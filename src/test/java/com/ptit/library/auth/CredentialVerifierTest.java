package com.ptit.library.auth;

import com.ptit.library.model.AuthSession;
import com.ptit.library.model.ReaderProfile;
import com.ptit.library.model.UserAccount;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CredentialVerifierTest {

    private static final String HASH = PasswordHasher.hash("Lib@12345");

    private final CredentialVerifier verifier = new CredentialVerifier();

    @Test
    void adminWithCorrectPassword_succeeds() {
        UserAccount admin = new UserAccount(1L, "admin", HASH, 1L, "ADMIN", false);
        CredentialVerifier.Result result = verifier.verify(admin, null, "Lib@12345");
        assertTrue(result.ok());
        AuthSession session = result.session();
        assertEquals(1L, session.userId());
        assertEquals("admin", session.username());
        assertEquals("ADMIN", session.role());
        assertNull(session.readerId());
    }

    @Test
    void pendingReader_canLogIn() {
        UserAccount user = new UserAccount(3L, "reader2", HASH, 3L, "READER", false);
        ReaderProfile reader = new ReaderProfile(2L, 3L, "DG000002", "Bình", "b@x.vn", "0901000002", "PENDING");
        CredentialVerifier.Result result = verifier.verify(user, reader, "Lib@12345");
        assertTrue(result.ok());
        assertEquals("READER", result.session().role());
        assertEquals("PENDING", result.session().readerStatus());
        assertEquals("DG000002", result.session().readerCode());
        assertEquals(2L, result.session().readerId());
    }

    @Test
    void unknownUser_isInvalidCredentials() {
        CredentialVerifier.Result result = verifier.verify(null, null, "x");
        assertFalse(result.ok());
        assertEquals(AuthFailure.INVALID_CREDENTIALS, result.failure());
    }

    @Test
    void wrongPassword_isInvalidCredentials() {
        UserAccount admin = new UserAccount(1L, "admin", HASH, 1L, "ADMIN", false);
        CredentialVerifier.Result result = verifier.verify(admin, null, "nope");
        assertFalse(result.ok());
        assertEquals(AuthFailure.INVALID_CREDENTIALS, result.failure());
    }

    @Test
    void lockedLibrarian_isInvalidCredentials() {
        UserAccount user = new UserAccount(2L, "librarian", HASH, 2L, "LIBRARIAN", true);
        CredentialVerifier.Result result = verifier.verify(user, null, "Lib@12345");
        assertFalse(result.ok());
        assertEquals(AuthFailure.INVALID_CREDENTIALS, result.failure());
    }

    @Test
    void rejectedReader_isRejected() {
        UserAccount user = new UserAccount(9L, "bad", HASH, 3L, "READER", false);
        ReaderProfile reader = new ReaderProfile(9L, 9L, "DG000099", "X", "x@x.vn", "0901000099", "REJECTED");
        CredentialVerifier.Result result = verifier.verify(user, reader, "Lib@12345");
        assertFalse(result.ok());
        assertEquals(AuthFailure.REJECTED, result.failure());
    }

    @Test
    void readerWithoutProfile_isInvalidCredentials() {
        UserAccount user = new UserAccount(3L, "reader1", HASH, 3L, "READER", false);
        CredentialVerifier.Result result = verifier.verify(user, null, "Lib@12345");
        assertFalse(result.ok());
        assertEquals(AuthFailure.INVALID_CREDENTIALS, result.failure());
    }
}
