package com.ptit.library.web;

import com.ptit.library.auth.AuthFailure;
import com.ptit.library.auth.CredentialVerifier;
import com.ptit.library.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginServlet extends HttpServlet {

    static final String AUTH_SERVICE = "authService";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("1".equals(req.getParameter("registered"))) {
            req.setAttribute("info", "Đăng ký thành công. Bạn có thể đăng nhập để tra cứu sách; mượn sách sau khi thủ thư duyệt thẻ.");
        }
        req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        AuthService authService = (AuthService) getServletContext().getAttribute(AUTH_SERVICE);
        if (authService == null) {
            req.setAttribute("error", "Hệ thống chưa sẵn sàng. Vui lòng thử lại.");
            req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
            return;
        }
        CredentialVerifier.Result result = authService.login(username, password);
        if (!result.ok()) {
            req.setAttribute("username", username);
            req.setAttribute("error", message(result.failure()));
            req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
            return;
        }
        SessionAuth.bind(req.getSession(true), result.session());
        resp.sendRedirect(req.getContextPath() + "/home");
    }

    static String message(AuthFailure failure) {
        if (failure == AuthFailure.REJECTED) {
            return "Tài khoản chưa được duyệt hoặc đã bị từ chối.";
        }
        return "Tên đăng nhập hoặc mật khẩu không đúng.";
    }
}
