package com.ptit.library.web;

import com.ptit.library.model.AuthSession;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

public final class RoleGuard {

    private RoleGuard() {
    }

    public static boolean hasRole(AuthSession auth, String... roles) {
        if (auth == null || roles == null || roles.length == 0) {
            return false;
        }
        return Arrays.asList(roles).contains(auth.role());
    }

    public static boolean require(HttpServletRequest req, HttpServletResponse resp, String... roles)
            throws IOException, ServletException {
        AuthSession auth = SessionAuth.get(req.getSession(false));
        if (hasRole(auth, roles)) {
            return true;
        }
        sendForbidden(req, resp);
        return false;
    }

    public static void sendForbidden(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        req.getRequestDispatcher("/WEB-INF/jsp/forbidden.jsp").forward(req, resp);
    }
}
