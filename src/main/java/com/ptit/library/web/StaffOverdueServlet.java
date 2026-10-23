package com.ptit.library.web;

import com.ptit.library.service.LoanService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// UI-14: danh sách quá hạn
public class StaffOverdueServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "ADMIN", "LIBRARIAN")) {
            return;
        }
        LoanService service = (LoanService) getServletContext().getAttribute(LoanService.ATTR_NAME);
        req.setAttribute("auth", SessionAuth.get(req.getSession(false)));
        req.setAttribute("lines", service.findLines("", true, true));
        req.getRequestDispatcher("/WEB-INF/jsp/staff-overdue.jsp").forward(req, resp);
    }
}
