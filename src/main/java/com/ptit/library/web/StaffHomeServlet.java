package com.ptit.library.web;

import com.ptit.library.model.AuthSession;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class StaffHomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "ADMIN", "LIBRARIAN")) {
            return;
        }
        AuthSession auth = SessionAuth.get(req.getSession(false));
        req.setAttribute("auth", auth);
        req.getRequestDispatcher("/WEB-INF/jsp/staff-home.jsp").forward(req, resp);
    }
}
