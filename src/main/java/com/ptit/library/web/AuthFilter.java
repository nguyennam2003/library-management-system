package com.ptit.library.web;

import com.ptit.library.model.AuthSession;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getRequestURI().substring(req.getContextPath().length());
        AuthSession auth = SessionAuth.get(req.getSession(false));
        if (isPublic(path)) {
            if (auth != null && isAuthForm(path)) {
                resp.sendRedirect(req.getContextPath() + "/home");
                return;
            }
            chain.doFilter(request, response);
            return;
        }
        if (auth == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        chain.doFilter(request, response);
    }

    static boolean isPublic(String path) {
        return path.equals("/")
                || path.equals("/index.jsp")
                || path.equals("/login")
                || path.equals("/register")
                || path.equals("/health")
                || path.equals("/logout");
    }

    static boolean isAuthForm(String path) {
        return path.equals("/login") || path.equals("/register");
    }

    @Override
    public void destroy() {
    }
}
