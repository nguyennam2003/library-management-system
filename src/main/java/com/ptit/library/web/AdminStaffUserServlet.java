package com.ptit.library.web;

import com.ptit.library.model.AuthSession;
import com.ptit.library.service.StaffUserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// UI-05: admin quản lý tài khoản nhân viên
public class AdminStaffUserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "ADMIN")) {
            return;
        }
        StaffUserService service = (StaffUserService) getServletContext().getAttribute(StaffUserService.ATTR_NAME);
        req.setAttribute("auth", SessionAuth.get(req.getSession(false)));
        Flash.consume(req);
        req.setAttribute("staff", service.list());
        long editId = Flash.parseLong(req.getParameter("editId"));
        if (editId > 0) {
            service.findStaff(editId).ifPresent(u -> req.setAttribute("editUser", u));
        }
        req.getRequestDispatcher("/WEB-INF/jsp/admin-staff-users.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "ADMIN")) {
            return;
        }
        AuthSession auth = SessionAuth.get(req.getSession(false));
        StaffUserService service = (StaffUserService) getServletContext().getAttribute(StaffUserService.ATTR_NAME);
        String action = req.getParameter("action");
        long id = Flash.parseLong(req.getParameter("id"));
        String error;
        String success;
        if ("create".equals(action)) {
            error = service.create(req.getParameter("username"), req.getParameter("password"), req.getParameter("role"));
            success = "Đã tạo tài khoản nhân viên.";
        } else if ("update".equals(action)) {
            error = service.update(id, req.getParameter("role"), req.getParameter("password"), auth.userId());
            success = "Đã cập nhật tài khoản.";
        } else if ("lock".equals(action)) {
            error = service.setLocked(id, true, auth.userId());
            success = "Đã khóa tài khoản.";
        } else if ("unlock".equals(action)) {
            error = service.setLocked(id, false, auth.userId());
            success = "Đã mở khóa tài khoản.";
        } else {
            error = "Thao tác không hợp lệ.";
            success = null;
        }
        Flash.set(req, error, success);
        resp.sendRedirect(req.getContextPath() + "/admin/staff-users");
    }
}
