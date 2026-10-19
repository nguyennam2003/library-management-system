package com.ptit.library.web;

import com.ptit.library.model.BookItem;
import com.ptit.library.service.BookCatalogService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

public class StaffBookCopyServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "ADMIN", "LIBRARIAN")) {
            return;
        }
        long bookId = parseLong(req.getParameter("bookId"));
        if (bookId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/staff/books");
            return;
        }
        BookCatalogService service = getService();
        Optional<BookItem> bookOpt = service.findBook(bookId);
        if (bookOpt.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/staff/books");
            return;
        }

        HttpSession session = req.getSession(false);
        req.setAttribute("auth", SessionAuth.get(session));
        consumeFlash(session, req);

        req.setAttribute("book", bookOpt.get());
        req.setAttribute("copies", service.listCopies(bookId));
        req.getRequestDispatcher("/WEB-INF/jsp/staff-book-copies.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "ADMIN", "LIBRARIAN")) {
            return;
        }
        long bookId = parseLong(req.getParameter("bookId"));
        if (bookId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/staff/books");
            return;
        }
        BookCatalogService service = getService();
        String action = req.getParameter("action");
        if (action == null) {
            action = "";
        }

        String error = null;
        String success = null;

        switch (action) {
            case "addCopy" -> {
                error = service.addCopy(bookId, req.getParameter("code"));
                if (error == null) {
                    success = "Đã thêm bản sách vật lý mới.";
                }
            }
            case "withdrawCopy" -> {
                long copyId = parseLong(req.getParameter("copyId"));
                error = service.withdrawCopy(copyId);
                if (error == null) {
                    success = "Đã ngừng lưu hành bản sách.";
                }
            }
            case "restoreCopy" -> {
                long copyId = parseLong(req.getParameter("copyId"));
                error = service.restoreCopy(copyId);
                if (error == null) {
                    success = "Đã mở lại lưu hành cho bản sách.";
                }
            }
            case "deleteCopy" -> {
                long copyId = parseLong(req.getParameter("copyId"));
                error = service.deleteCopy(copyId);
                if (error == null) {
                    success = "Đã xóa bản sách.";
                }
            }
            default -> error = "Thao tác không hợp lệ.";
        }

        HttpSession session = req.getSession();
        if (error != null) {
            session.setAttribute("flashError", error);
        } else if (success != null) {
            session.setAttribute("flashSuccess", success);
        }
        resp.sendRedirect(req.getContextPath() + "/staff/books/copies?bookId=" + bookId);
    }

    private BookCatalogService getService() {
        return (BookCatalogService) getServletContext().getAttribute(BookCatalogService.ATTR_NAME);
    }

    private static void consumeFlash(HttpSession session, HttpServletRequest req) {
        if (session == null) {
            return;
        }
        Object err = session.getAttribute("flashError");
        if (err != null) {
            req.setAttribute("error", err);
            session.removeAttribute("flashError");
        }
        Object ok = session.getAttribute("flashSuccess");
        if (ok != null) {
            req.setAttribute("info", ok);
            session.removeAttribute("flashSuccess");
        }
    }

    private static long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return -1L;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return -1L;
        }
    }
}
