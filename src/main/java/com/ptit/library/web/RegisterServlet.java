package com.ptit.library.web;

import com.ptit.library.auth.RegistrationInput;
import com.ptit.library.service.RegistrationResult;
import com.ptit.library.service.RegistrationService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RegisterServlet extends HttpServlet {

    static final String REGISTRATION_SERVICE = "registrationService";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RegistrationInput input = new RegistrationInput(
                req.getParameter("username"),
                req.getParameter("password"),
                req.getParameter("confirmPassword"),
                req.getParameter("fullName"),
                req.getParameter("email"),
                req.getParameter("phone"));
        req.setAttribute("input", input);
        RegistrationService service = (RegistrationService) getServletContext().getAttribute(REGISTRATION_SERVICE);
        if (service == null) {
            req.setAttribute("errors", java.util.List.of("Hệ thống chưa sẵn sàng. Vui lòng thử lại."));
            req.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(req, resp);
            return;
        }
        RegistrationResult result = service.register(input);
        if (!result.ok()) {
            req.setAttribute("errors", result.errors());
            req.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(req, resp);
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/login?registered=1");
    }
}
