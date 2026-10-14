package com.ptit.library.web;

import com.ptit.library.model.AuthSession;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AuthSession auth = SessionAuth.get(req.getSession(false));
        if (auth == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        req.setAttribute("auth", auth);
        String view = auth.isStaff() ? "/WEB-INF/jsp/staff-home.jsp" : "/WEB-INF/jsp/reader-home.jsp";
        req.getRequestDispatcher(view).forward(req, resp);
    }
}
