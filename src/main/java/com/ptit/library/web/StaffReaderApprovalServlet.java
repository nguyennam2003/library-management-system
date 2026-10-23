package com.ptit.library.web;

import com.ptit.library.service.StaffReaderService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// UI-10: hàng đợi duyệt thẻ độc giả
public class StaffReaderApprovalServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "ADMIN", "LIBRARIAN")) {
            return;
        }
        StaffReaderService service = (StaffReaderService) getServletContext().getAttribute(StaffReaderService.ATTR_NAME);
        req.setAttribute("auth", SessionAuth.get(req.getSession(false)));
        Flash.consume(req);
        req.setAttribute("queue", service.listQueue());
        req.getRequestDispatcher("/WEB-INF/jsp/staff-reader-pending.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "ADMIN", "LIBRARIAN")) {
            return;
        }
        StaffReaderService service = (StaffReaderService) getServletContext().getAttribute(StaffReaderService.ATTR_NAME);
        String action = req.getParameter("action");
        long id = Flash.parseLong(req.getParameter("id"));
        String error = service.changeStatus(id, action);
        Flash.set(req, error, "Đã cập nhật trạng thái hồ sơ.");
        resp.sendRedirect(req.getContextPath() + "/staff/readers/pending");
    }
}
