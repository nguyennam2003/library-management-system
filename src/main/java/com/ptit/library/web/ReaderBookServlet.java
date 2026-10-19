package com.ptit.library.web;

import com.ptit.library.model.BookItem;
import com.ptit.library.service.BookCatalogService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class ReaderBookServlet extends HttpServlet {

    private static final int PAGE_SIZE = 8;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.require(req, resp, "READER")) {
            return;
        }
        req.setAttribute("auth", SessionAuth.get(req.getSession(false)));
        BookCatalogService service = getService();

        Long detailId = parseLongObj(req.getParameter("id"));
        if (detailId != null && detailId > 0) {
            Optional<BookItem> bookOpt = service.findBook(detailId);
            if (bookOpt.isEmpty() || !bookOpt.get().isActive()) {
                req.setAttribute("error", "Không tìm thấy đầu sách hoặc đầu sách đã ngừng lưu hành.");
            } else {
                req.setAttribute("book", bookOpt.get());
                req.setAttribute("copies", service.listCopies(detailId));
            }
            req.getRequestDispatcher("/WEB-INF/jsp/reader-book-detail.jsp").forward(req, resp);
            return;
        }

        String q = req.getParameter("q");
        Long categoryId = parseLongObj(req.getParameter("categoryId"));
        boolean onlyAvailable = "1".equals(req.getParameter("onlyAvailable"));
        int page = parseInt(req.getParameter("page"), 1);

        BookCatalogService.PageResult pageResult = service.searchBooks(
                q, categoryId, onlyAvailable, true, page, PAGE_SIZE);

        req.setAttribute("categories", service.listCategories());
        req.setAttribute("pageResult", pageResult);
        req.setAttribute("q", q == null ? "" : q);
        req.setAttribute("categoryId", categoryId);
        req.setAttribute("onlyAvailable", onlyAvailable);
        req.getRequestDispatcher("/WEB-INF/jsp/reader-books.jsp").forward(req, resp);
    }

    private BookCatalogService getService() {
        return (BookCatalogService) getServletContext().getAttribute(BookCatalogService.ATTR_NAME);
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

    private static int parseInt(String value, int defaultVal) {
        if (value == null || value.isBlank()) {
            return defaultVal;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
