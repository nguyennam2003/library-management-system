package com.ptit.library.web;

import com.ptit.library.service.SettingService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// UI-06: admin cấu hình hệ thống
public class AdminSettingsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "ADMIN")) {
            return;
        }
        SettingService service = (SettingService) getServletContext().getAttribute(SettingService.ATTR_NAME);
        req.setAttribute("auth", SessionAuth.get(req.getSession(false)));
        Flash.consume(req);
        req.setAttribute("maxBorrow", service.getMaxBorrowCount());
        req.setAttribute("loanDays", service.getLoanPeriodDays());
        req.getRequestDispatcher("/WEB-INF/jsp/admin-settings.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "ADMIN")) {
            return;
        }
        SettingService service = (SettingService) getServletContext().getAttribute(SettingService.ATTR_NAME);
        String error = service.save(String.valueOf(req.getParameter("maxBorrow")), String.valueOf(req.getParameter("loanDays")));
        Flash.set(req, error, "Đã lưu cấu hình. Giá trị mới áp dụng cho phiếu mượn tạo sau này.");
        resp.sendRedirect(req.getContextPath() + "/admin/settings");
    }
}
