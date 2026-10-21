package com.ptit.library.web;

import com.ptit.library.service.LoanService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// UI-13: tra cứu phiếu mượn và xác nhận trả sách
public class StaffLoanReturnServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "ADMIN", "LIBRARIAN")) {
            return;
        }
        LoanService service = (LoanService) getServletContext().getAttribute(LoanService.ATTR_NAME);
        req.setAttribute("auth", SessionAuth.get(req.getSession(false)));
        Flash.consume(req);
        String q = req.getParameter("q");
        boolean onlyBorrowed = "1".equals(req.getParameter("onlyBorrowed"));
        req.setAttribute("q", q == null ? "" : q);
        req.setAttribute("onlyBorrowed", onlyBorrowed);
        req.setAttribute("lines", service.findLines(q, onlyBorrowed, false));
        req.getRequestDispatcher("/WEB-INF/jsp/staff-loans.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "ADMIN", "LIBRARIAN")) {
            return;
        }
        LoanService service = (LoanService) getServletContext().getAttribute(LoanService.ATTR_NAME);
        String error = service.returnCopy(Flash.parseLong(req.getParameter("detailId")));
        Flash.set(req, error, "Đã xác nhận trả sách.");
        String q = req.getParameter("q");
        String back = req.getContextPath() + "/staff/loans?q=" + URLEncoder.encode(q == null ? "" : q, StandardCharsets.UTF_8);
        if ("1".equals(req.getParameter("onlyBorrowed"))) {
            back += "&onlyBorrowed=1";
        }
        resp.sendRedirect(back);
    }
}
