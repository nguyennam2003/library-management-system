package com.ptit.library.web;

import com.ptit.library.auth.AuthFailure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthFilterTest {

    @Test
    void publicPaths() {
        assertTrue(AuthFilter.isPublic("/login"));
        assertTrue(AuthFilter.isPublic("/register"));
        assertTrue(AuthFilter.isPublic("/health"));
        assertTrue(AuthFilter.isPublic("/logout"));
        assertTrue(AuthFilter.isPublic("/"));
        assertFalse(AuthFilter.isPublic("/home"));
        assertTrue(AuthFilter.isAuthForm("/login"));
        assertFalse(AuthFilter.isAuthForm("/home"));
    }

    @Test
    void loginMessages() {
        assertEquals(
                "Tài khoản chưa được duyệt hoặc đã bị từ chối.",
                LoginServlet.message(AuthFailure.REJECTED));
        assertEquals(
                "Tên đăng nhập hoặc mật khẩu không đúng.",
                LoginServlet.message(AuthFailure.INVALID_CREDENTIALS));
    }
}
