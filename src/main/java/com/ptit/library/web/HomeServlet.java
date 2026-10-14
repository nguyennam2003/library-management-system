package com.ptit.library.web;

import com.ptit.library.model.AuthSession;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthSession auth = SessionAuth.get(req.getSession(false));
        if (auth == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        String target = auth.isReader() ? "/reader/home" : "/staff/home";
        resp.sendRedirect(req.getContextPath() + target);
    }
}
