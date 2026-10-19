package com.ptit.library.web;

import com.ptit.library.model.BookItem;
import com.ptit.library.service.BookCatalogService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StaffBookServlet extends HttpServlet {

    private static final int PAGE_SIZE = 8;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "ADMIN", "LIBRARIAN")) {
            return;
        }
        HttpSession session = req.getSession(false);
        req.setAttribute("auth", SessionAuth.get(session));
        consumeFlash(session, req);

        BookCatalogService service = getService();
        loadSearchAndCatalog(req, service);

        long editId = parseLong(req.getParameter("editId"));
        if (editId > 0) {
            Optional<BookItem> editBook = service.findBook(editId);
            editBook.ifPresent(b -> req.setAttribute("editBook", b));
        }

        req.getRequestDispatcher("/WEB-INF/jsp/staff-books.jsp").forward(req, resp);
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

        if ("create".equals(action) || "update".equals(action)) {
            handleSaveBook(req, resp, service, "update".equals(action));
            return;
        }

        HttpSession session = req.getSession();
        if ("toggleStatus".equals(action)) {
            long bookId = parseLong(req.getParameter("id"));
            String err = service.toggleBookStatus(bookId);
            if (err != null) {
                session.setAttribute("flashError", err);
            } else {
                session.setAttribute("flashSuccess", "Đã cập nhật trạng thái lưu hành của đầu sách.");
            }
        } else if ("delete".equals(action)) {
            long bookId = parseLong(req.getParameter("id"));
            String err = service.deleteBook(bookId);
            if (err != null) {
                session.setAttribute("flashError", err);
            } else {
                session.setAttribute("flashSuccess", "Đã xóa đầu sách khỏi hệ thống.");
            }
        } else {
            session.setAttribute("flashError", "Thao tác không hợp lệ.");
        }

        resp.sendRedirect(req.getContextPath() + "/staff/books");
    }

    private void handleSaveBook(
            HttpServletRequest req,
            HttpServletResponse resp,
            BookCatalogService service,
            boolean isUpdate) throws ServletException, IOException {
        long bookId = parseLong(req.getParameter("id"));
        String title = req.getParameter("title");
        String isbn = req.getParameter("isbn");
        Long categoryId = parseLongObj(req.getParameter("categoryId"));
        Integer publishYear = parseIntObj(req.getParameter("publishYear"));
        String publisher = req.getParameter("publisher");
        List<Long> authorIds = parseLongList(req.getParameterValues("authorIds"));

        List<String> errors = isUpdate
                ? service.updateBook(bookId, title, isbn, categoryId, authorIds, publisher, publishYear)
                : service.createBook(title, isbn, categoryId, authorIds, publisher, publishYear);

        if (!errors.isEmpty()) {
            req.setAttribute("auth", SessionAuth.get(req.getSession(false)));
            req.setAttribute("errors", errors);
            req.setAttribute("formId", isUpdate ? bookId : null);
            req.setAttribute("formTitle", title);
            req.setAttribute("formIsbn", isbn);
            req.setAttribute("formCategoryId", categoryId);
            req.setAttribute("formPublisher", publisher);
            req.setAttribute("formPublishYear", req.getParameter("publishYear"));
            req.setAttribute("formAuthorIds", authorIds);
            loadSearchAndCatalog(req, service);
            req.getRequestDispatcher("/WEB-INF/jsp/staff-books.jsp").forward(req, resp);
            return;
        }

        HttpSession session = req.getSession();
        session.setAttribute("flashSuccess", isUpdate
                ? "Đã cập nhật thông tin đầu sách thành công."
                : "Đã thêm đầu sách mới thành công.");
        resp.sendRedirect(req.getContextPath() + "/staff/books");
    }

    private void loadSearchAndCatalog(HttpServletRequest req, BookCatalogService service) {
        String q = req.getParameter("q");
        Long categoryId = parseLongObj(req.getParameter("categoryIdFilter"));
        boolean onlyAvailable = "1".equals(req.getParameter("onlyAvailable"));
        int page = parseIntObj(req.getParameter("page")) == null ? 1 : parseIntObj(req.getParameter("page"));

        BookCatalogService.PageResult pageResult = service.searchBooks(
                q, categoryId, onlyAvailable, false, page, PAGE_SIZE);

        req.setAttribute("categories", service.listCategories());
        req.setAttribute("authors", service.listAuthors());
        req.setAttribute("pageResult", pageResult);
        req.setAttribute("q", q == null ? "" : q);
        req.setAttribute("categoryIdFilter", categoryId);
        req.setAttribute("onlyAvailable", onlyAvailable);
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
        Long l = parseLongObj(value);
        return l == null ? -1L : l;
    }

    private static Long parseLongObj(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer parseIntObj(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static List<Long> parseLongList(String[] values) {
        List<Long> result = new ArrayList<>();
        if (values == null) {
            return result;
        }
        for (String v : values) {
            Long id = parseLongObj(v);
            if (id != null && id > 0) {
                result.add(id);
            }
        }
        return result;
    }
}
