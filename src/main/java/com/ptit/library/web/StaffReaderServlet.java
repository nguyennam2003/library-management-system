package com.ptit.library.web;

import com.ptit.library.service.StaffReaderService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// UI-11: tìm / tạo / sửa / khóa độc giả
public class StaffReaderServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "ADMIN", "LIBRARIAN")) {
            return;
        }
        StaffReaderService service = (StaffReaderService) getServletContext().getAttribute(StaffReaderService.ATTR_NAME);
        req.setAttribute("auth", SessionAuth.get(req.getSession(false)));
        Flash.consume(req);
        String q = req.getParameter("q");
        String status = req.getParameter("status");
        req.setAttribute("q", q == null ? "" : q);
        req.setAttribute("status", status == null ? "" : status);
        req.setAttribute("readers", service.search(q, status));
        long editId = Flash.parseLong(req.getParameter("editId"));
        if (editId > 0) {
            service.findById(editId).ifPresent(r -> req.setAttribute("editReader", r));
        }
        req.getRequestDispatcher("/WEB-INF/jsp/staff-readers.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "ADMIN", "LIBRARIAN")) {
            return;
        }
        StaffReaderService service = (StaffReaderService) getServletContext().getAttribute(StaffReaderService.ATTR_NAME);
        String action = req.getParameter("action");
        String error;
        String success;
        if ("create".equals(action)) {
            error = service.createAtCounter(req.getParameter("username"), req.getParameter("password"),
                    req.getParameter("fullName"), req.getParameter("email"), req.getParameter("phone"));
            success = "Đã tạo độc giả mới (thẻ ACTIVE).";
        } else if ("update".equals(action)) {
            error = service.update(Flash.parseLong(req.getParameter("id")), req.getParameter("fullName"),
                    req.getParameter("email"), req.getParameter("phone"));
            success = "Đã cập nhật thông tin độc giả.";
        } else {
            error = service.changeStatus(Flash.parseLong(req.getParameter("id")), action);
            success = "Đã cập nhật trạng thái thẻ.";
        }
        Flash.set(req, error, success);
        resp.sendRedirect(req.getContextPath() + "/staff/readers");
    }
}
