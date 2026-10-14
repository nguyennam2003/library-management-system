package com.ptit.library.web;

import com.ptit.library.model.AuthSession;

import javax.servlet.http.HttpSession;

public final class SessionAuth {

    public static final String AUTH = "authSession";

    private SessionAuth() {
    }

    public static void bind(HttpSession session, AuthSession auth) {
        session.setAttribute("userId", auth.userId());
        session.setAttribute("username", auth.username());
        session.setAttribute("role", auth.role());
        session.setAttribute("readerId", auth.readerId());
        session.setAttribute("readerStatus", auth.readerStatus());
        session.setAttribute("readerCode", auth.readerCode());
        session.setAttribute(AUTH, auth);
    }

    public static AuthSession get(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(AUTH);
        return value instanceof AuthSession ? (AuthSession) value : null;
    }
}
