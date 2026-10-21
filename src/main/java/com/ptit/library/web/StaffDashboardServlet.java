package com.ptit.library.web;

import com.ptit.library.service.DashboardService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

// UI-04: dashboard thống kê
public class StaffDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "ADMIN", "LIBRARIAN")) {
            return;
        }
        DashboardService service = (DashboardService) getServletContext().getAttribute(DashboardService.ATTR_NAME);
        req.setAttribute("auth", SessionAuth.get(req.getSession(false)));

        LocalDate from = parseDate(req.getParameter("from"));
        LocalDate to = parseDate(req.getParameter("to"));
        if (from != null && to != null && from.isAfter(to)) {
            req.setAttribute("error", "Ngày bắt đầu phải trước ngày kết thúc.");
            from = null;
            to = null;
        }
        req.setAttribute("from", from == null ? "" : from.toString());
        req.setAttribute("to", to == null ? "" : to.toString());

        req.setAttribute("totalBooks", service.totalBooks());
        req.setAttribute("totalCopies", service.totalCopies());
        req.setAttribute("activeReaders", service.activeReaders());
        req.setAttribute("borrowedCopies", service.borrowedCopies());
        req.setAttribute("overdueCount", service.overdueCount());
        req.setAttribute("rangeCount", service.borrowCountInRange(from, to));
        req.setAttribute("topBooks", service.topBooks(from, to));
        req.getRequestDispatcher("/WEB-INF/jsp/staff-dashboard.jsp").forward(req, resp);
    }

    private static LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
