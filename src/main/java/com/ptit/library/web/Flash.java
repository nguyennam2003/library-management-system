package com.ptit.library.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

// thông báo hiện 1 lần sau khi redirect + hàm đọc số
public final class Flash {

    private Flash() {
    }

    public static void set(HttpServletRequest req, String error, String success) {
        HttpSession session = req.getSession();
        if (error != null) {
            session.setAttribute("flashError", error);
        } else if (success != null) {
            session.setAttribute("flashSuccess", success);
        }
    }

    public static void consume(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return;
        }
        Object err = session.getAttribute("flashError");
        if (err != null) {
            req.setAttribute("error", err);
            session.removeAttribute("flashError");
        }
        Object ok = session.getAttribute("flashSuccess");
        if (ok != null) {
            req.setAttribute("info", ok);
            session.removeAttribute("flashSuccess");
        }
    }

    public static long parseLong(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (Exception e) {
            return -1L;
        }
    }
}
