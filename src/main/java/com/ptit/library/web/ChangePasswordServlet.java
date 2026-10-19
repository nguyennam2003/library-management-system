package com.ptit.library.web;

import com.ptit.library.model.AuthSession;
import com.ptit.library.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ChangePasswordServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "ADMIN", "LIBRARIAN", "READER")) {
            return;
        }
        AuthSession auth = SessionAuth.get(req.getSession(false));
        req.setAttribute("auth", auth);
        if ("1".equals(req.getParameter("changed"))) {
            req.setAttribute("info", "Đổi mật khẩu thành công.");
        }
        req.getRequestDispatcher("/WEB-INF/jsp/change-password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "ADMIN", "LIBRARIAN", "READER")) {
            return;
        }
        AuthSession auth = SessionAuth.get(req.getSession(false));
        req.setAttribute("auth", auth);

        AuthService authService = (AuthService) getServletContext().getAttribute(LoginServlet.AUTH_SERVICE);
        if (authService == null) {
            req.setAttribute("error", "Hệ thống chưa sẵn sàng. Vui lòng thử lại.");
            req.getRequestDispatcher("/WEB-INF/jsp/change-password.jsp").forward(req, resp);
            return;
        }

        String currentPassword = req.getParameter("currentPassword");
        String newPassword = req.getParameter("newPassword");
        String confirmPassword = req.getParameter("confirmPassword");

        String error = authService.changePassword(auth.userId(), currentPassword, newPassword, confirmPassword);
        if (error != null) {
            req.setAttribute("error", error);
            req.getRequestDispatcher("/WEB-INF/jsp/change-password.jsp").forward(req, resp);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/account/password?changed=1");
    }
}
