package com.ptit.library.web;

import com.ptit.library.model.Author;
import com.ptit.library.model.Category;
import com.ptit.library.service.BookCatalogService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

public class StaffCatalogServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "ADMIN", "LIBRARIAN")) {
            return;
        }
        HttpSession session = req.getSession(false);
        req.setAttribute("auth", SessionAuth.get(session));
        consumeFlash(session, req);

        BookCatalogService service = getService();
        req.setAttribute("categories", service.listCategories());
        req.setAttribute("authors", service.listAuthors());

        long editCategoryId = parseLong(req.getParameter("editCategoryId"));
        if (editCategoryId > 0) {
            Optional<Category> cat = service.findCategory(editCategoryId);
            cat.ifPresent(c -> req.setAttribute("editCategory", c));
        }

        long editAuthorId = parseLong(req.getParameter("editAuthorId"));
        if (editAuthorId > 0) {
            Optional<Author> author = service.findAuthor(editAuthorId);
            author.ifPresent(a -> req.setAttribute("editAuthor", a));
        }

        req.getRequestDispatcher("/WEB-INF/jsp/staff-catalog.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "ADMIN", "LIBRARIAN")) {
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
            case "createCategory" -> {
                error = service.createCategory(req.getParameter("name"));
                if (error == null) {
                    success = "Đã thêm thể loại mới thành công.";
                }
            }
            case "updateCategory" -> {
                long id = parseLong(req.getParameter("id"));
                error = service.updateCategory(id, req.getParameter("name"));
                if (error == null) {
                    success = "Đã cập nhật tên thể loại.";
                }
            }
            case "deleteCategory" -> {
                long id = parseLong(req.getParameter("id"));
                error = service.deleteCategory(id);
                if (error == null) {
                    success = "Đã xóa thể loại.";
                }
            }
            case "createAuthor" -> {
                error = service.createAuthor(req.getParameter("name"));
                if (error == null) {
                    success = "Đã thêm tác giả mới thành công.";
                }
            }
            case "updateAuthor" -> {
                long id = parseLong(req.getParameter("id"));
                error = service.updateAuthor(id, req.getParameter("name"));
                if (error == null) {
                    success = "Đã cập nhật thông tin tác giả.";
                }
            }
            case "deleteAuthor" -> {
                long id = parseLong(req.getParameter("id"));
                error = service.deleteAuthor(id);
                if (error == null) {
                    success = "Đã xóa tác giả.";
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
        resp.sendRedirect(req.getContextPath() + "/staff/catalog");
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
