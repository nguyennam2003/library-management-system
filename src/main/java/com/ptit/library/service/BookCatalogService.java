package com.ptit.library.service;

import com.ptit.library.dao.AuthorDao;
import com.ptit.library.dao.BookCopyDao;
import com.ptit.library.dao.BookDao;
import com.ptit.library.dao.CategoryDao;
import com.ptit.library.dao.ConnectionFactory;
import com.ptit.library.model.Author;
import com.ptit.library.model.BookCopy;
import com.ptit.library.model.BookItem;
import com.ptit.library.model.Category;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

public final class BookCatalogService {

    public static final String ATTR_NAME = "bookCatalogService";

    public record PageResult(
            List<BookItem> items,
            int totalItems,
            int page,
            int pageSize,
            int totalPages
    ) {
    }

    private final ConnectionFactory connections;
    private final CategoryDao categoryDao = new CategoryDao();
    private final AuthorDao authorDao = new AuthorDao();
    private final BookDao bookDao = new BookDao();
    private final BookCopyDao copyDao = new BookCopyDao();
    private final BookValidator validator = new BookValidator();

    public BookCatalogService(ConnectionFactory connections) {
        this.connections = connections;
    }

    public List<Category> listCategories() {
        try (Connection conn = connections.getConnection()) {
            return categoryDao.findAll(conn);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách thể loại", e);
        }
    }

    public Optional<Category> findCategory(long id) {
        try (Connection conn = connections.getConnection()) {
            return categoryDao.findById(conn, id);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải thể loại", e);
        }
    }

    public String createCategory(String rawName) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isEmpty() || name.length() > 128) {
            return "Tên thể loại không được để trống và tối đa 128 ký tự.";
        }
        try (Connection conn = connections.getConnection()) {
            if (categoryDao.existsByName(conn, name, null)) {
                return "Tên thể loại đã tồn tại.";
            }
            categoryDao.insert(conn, name);
            return null;
        } catch (SQLException e) {
            return "Không thể thêm thể loại. Vui lòng thử lại.";
        }
    }

    public String updateCategory(long id, String rawName) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isEmpty() || name.length() > 128) {
            return "Tên thể loại không được để trống và tối đa 128 ký tự.";
        }
        try (Connection conn = connections.getConnection()) {
            if (categoryDao.findById(conn, id).isEmpty()) {
                return "Thể loại không tồn tại.";
            }
            if (categoryDao.existsByName(conn, name, id)) {
                return "Tên thể loại đã tồn tại.";
            }
            categoryDao.update(conn, id, name);
            return null;
        } catch (SQLException e) {
            return "Không thể cập nhật thể loại. Vui lòng thử lại.";
        }
    }

    public String deleteCategory(long id) {
        try (Connection conn = connections.getConnection()) {
            if (categoryDao.countBooks(conn, id) > 0) {
                return "Không thể xóa thể loại đang được đầu sách sử dụng.";
            }
            categoryDao.delete(conn, id);
            return null;
        } catch (SQLException e) {
            return "Không thể xóa thể loại.";
        }
    }

    public List<Author> listAuthors() {
        try (Connection conn = connections.getConnection()) {
            return authorDao.findAll(conn);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách tác giả", e);
        }
    }

    public Optional<Author> findAuthor(long id) {
        try (Connection conn = connections.getConnection()) {
            return authorDao.findById(conn, id);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải tác giả", e);
        }
    }

    public String createAuthor(String rawName) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isEmpty() || name.length() > 128) {
            return "Tên tác giả không được để trống và tối đa 128 ký tự.";
        }
        try (Connection conn = connections.getConnection()) {
            authorDao.insert(conn, name);
            return null;
        } catch (SQLException e) {
            return "Không thể thêm tác giả. Vui lòng thử lại.";
        }
    }

    public String updateAuthor(long id, String rawName) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isEmpty() || name.length() > 128) {
            return "Tên tác giả không được để trống và tối đa 128 ký tự.";
        }
        try (Connection conn = connections.getConnection()) {
            if (authorDao.findById(conn, id).isEmpty()) {
                return "Tác giả không tồn tại.";
            }
            authorDao.update(conn, id, name);
            return null;
        } catch (SQLException e) {
            return "Không thể cập nhật tác giả. Vui lòng thử lại.";
        }
    }

    public String deleteAuthor(long id) {
        try (Connection conn = connections.getConnection()) {
            if (authorDao.countBooks(conn, id) > 0) {
                return "Không thể xóa tác giả đang được đầu sách sử dụng.";
            }
            authorDao.delete(conn, id);
            return null;
        } catch (SQLException e) {
            return "Không thể xóa tác giả.";
        }
    }

    public PageResult searchBooks(
            String keyword,
            Long categoryId,
            boolean onlyAvailable,
            boolean onlyActiveBooks,
            int page,
            int pageSize) {
        int safePageSize = pageSize <= 0 ? 10 : pageSize;
        try (Connection conn = connections.getConnection()) {
            int total = bookDao.countSearch(conn, keyword, categoryId, onlyAvailable, onlyActiveBooks);
            int totalPages = Math.max(1, (int) Math.ceil((double) total / safePageSize));
            int safePage = Math.min(Math.max(1, page), totalPages);
            List<BookItem> items = bookDao.search(
                    conn, keyword, categoryId, onlyAvailable, onlyActiveBooks, safePage, safePageSize);
            return new PageResult(items, total, safePage, safePageSize, totalPages);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tra cứu danh sách sách", e);
        }
    }

    public Optional<BookItem> findBook(long id) {
        try (Connection conn = connections.getConnection()) {
            return bookDao.findById(conn, id);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải thông tin đầu sách", e);
        }
    }

    public List<String> createBook(
            String title,
            String rawIsbn,
            Long categoryId,
            List<Long> authorIds,
            String publisher,
            Integer publishYear) {
        List<Long> distinctAuthors = deduplicateIds(authorIds);
        List<String> errors = validator.validateBook(
                title, rawIsbn, categoryId, distinctAuthors, publisher, publishYear);
        if (!errors.isEmpty()) {
            return errors;
        }
        String normalizedIsbn = BookValidator.normalizeIsbn(rawIsbn);
        try (Connection conn = connections.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (normalizedIsbn != null && bookDao.existsByIsbn(conn, normalizedIsbn, null)) {
                    conn.rollback();
                    return List.of("Mã ISBN này đã tồn tại trên hệ thống.");
                }
                if (categoryDao.findById(conn, categoryId).isEmpty()) {
                    conn.rollback();
                    return List.of("Thể loại đã chọn không tồn tại.");
                }
                long bookId = bookDao.insert(
                        conn, title.trim(), normalizedIsbn, categoryId, publisher.trim(), publishYear);
                bookDao.replaceBookAuthors(conn, bookId, distinctAuthors);
                conn.commit();
                return List.of();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return List.of("Không thể thêm đầu sách. Vui lòng kiểm tra lại dữ liệu.");
        }
    }

    public List<String> updateBook(
            long bookId,
            String title,
            String rawIsbn,
            Long categoryId,
            List<Long> authorIds,
            String publisher,
            Integer publishYear) {
        List<Long> distinctAuthors = deduplicateIds(authorIds);
        List<String> errors = validator.validateBook(
                title, rawIsbn, categoryId, distinctAuthors, publisher, publishYear);
        if (!errors.isEmpty()) {
            return errors;
        }
        String normalizedIsbn = BookValidator.normalizeIsbn(rawIsbn);
        try (Connection conn = connections.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (bookDao.findById(conn, bookId).isEmpty()) {
                    conn.rollback();
                    return List.of("Đầu sách không tồn tại.");
                }
                if (normalizedIsbn != null && bookDao.existsByIsbn(conn, normalizedIsbn, bookId)) {
                    conn.rollback();
                    return List.of("Mã ISBN này đã được sử dụng cho đầu sách khác.");
                }
                bookDao.update(
                        conn, bookId, title.trim(), normalizedIsbn, categoryId, publisher.trim(), publishYear);
                bookDao.replaceBookAuthors(conn, bookId, distinctAuthors);
                conn.commit();
                return List.of();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return List.of("Không thể cập nhật đầu sách. Vui lòng thử lại.");
        }
    }

    public String toggleBookStatus(long bookId) {
        try (Connection conn = connections.getConnection()) {
            Optional<BookItem> bookOpt = bookDao.findById(conn, bookId);
            if (bookOpt.isEmpty()) {
                return "Đầu sách không tồn tại.";
            }
            BookItem book = bookOpt.get();
            String nextStatus = "ACTIVE".equals(book.status()) ? "WITHDRAWN" : "ACTIVE";
            bookDao.updateStatus(conn, bookId, nextStatus);
            return null;
        } catch (SQLException e) {
            return "Không thể thay đổi trạng thái lưu hành của đầu sách.";
        }
    }

    public String deleteBook(long bookId) {
        try (Connection conn = connections.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (bookDao.findById(conn, bookId).isEmpty()) {
                    conn.rollback();
                    return "Đầu sách không tồn tại.";
                }
                if (bookDao.hasLoanHistory(conn, bookId)) {
                    conn.rollback();
                    return "Không thể xóa vật lý đầu sách đã từng phát sinh lịch sử mượn/trả. Vui lòng dùng chức năng Ngừng lưu hành.";
                }
                if (bookDao.hasBorrowedCopy(conn, bookId)) {
                    conn.rollback();
                    return "Không thể xóa đầu sách đang có bản sách được mượn.";
                }
                bookDao.delete(conn, bookId);
                conn.commit();
                return null;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return "Không thể xóa đầu sách.";
        }
    }

    public List<BookCopy> listCopies(long bookId) {
        try (Connection conn = connections.getConnection()) {
            return copyDao.findByBookId(conn, bookId);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách bản sách", e);
        }
    }

    public String addCopy(long bookId, String rawCode) {
        String err = validator.validateCopyCode(rawCode);
        if (err != null) {
            return err;
        }
        String code = rawCode.trim();
        try (Connection conn = connections.getConnection()) {
            if (bookDao.findById(conn, bookId).isEmpty()) {
                return "Đầu sách không tồn tại.";
            }
            if (copyDao.existsByCode(conn, code)) {
                return "Mã bản sách '" + code + "' đã tồn tại.";
            }
            copyDao.insert(conn, bookId, code);
            return null;
        } catch (SQLException e) {
            return "Không thể thêm bản sách mới.";
        }
    }

    public String withdrawCopy(long copyId) {
        try (Connection conn = connections.getConnection()) {
            Optional<BookCopy> copyOpt = copyDao.findById(conn, copyId);
            if (copyOpt.isEmpty()) {
                return "Bản sách không tồn tại.";
            }
            BookCopy copy = copyOpt.get();
            if ("BORROWED".equals(copy.status())) {
                return "Không thể ngừng lưu hành bản sách đang được mượn (BORROWED).";
            }
            if (!"AVAILABLE".equals(copy.status())) {
                return "Chỉ có thể ngừng lưu hành bản sách đang ở trạng thái AVAILABLE.";
            }
            copyDao.updateStatus(conn, copyId, "WITHDRAWN");
            return null;
        } catch (SQLException e) {
            return "Không thể cập nhật trạng thái bản sách.";
        }
    }

    public String restoreCopy(long copyId) {
        try (Connection conn = connections.getConnection()) {
            Optional<BookCopy> copyOpt = copyDao.findById(conn, copyId);
            if (copyOpt.isEmpty()) {
                return "Bản sách không tồn tại.";
            }
            BookCopy copy = copyOpt.get();
            if (!"WITHDRAWN".equals(copy.status())) {
                return "Chỉ có thể mở lại lưu hành cho bản sách đang ngừng lưu hành (WITHDRAWN).";
            }
            copyDao.updateStatus(conn, copyId, "AVAILABLE");
            return null;
        } catch (SQLException e) {
            return "Không thể mở lại lưu hành bản sách.";
        }
    }

    public String deleteCopy(long copyId) {
        try (Connection conn = connections.getConnection()) {
            Optional<BookCopy> copyOpt = copyDao.findById(conn, copyId);
            if (copyOpt.isEmpty()) {
                return "Bản sách không tồn tại.";
            }
            BookCopy copy = copyOpt.get();
            if ("BORROWED".equals(copy.status())) {
                return "Không thể xóa bản sách đang được mượn.";
            }
            if (copy.hasLoanHistory()) {
                return "Không thể xóa vật lý bản sách đã từng phát sinh mượn/trả. Vui lòng dùng Ngừng lưu hành.";
            }
            copyDao.delete(conn, copyId);
            return null;
        } catch (SQLException e) {
            return "Không thể xóa bản sách.";
        }
    }

    private static List<Long> deduplicateIds(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        LinkedHashSet<Long> set = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id != null && id > 0) {
                set.add(id);
            }
        }
        return new ArrayList<>(set);
    }
}
