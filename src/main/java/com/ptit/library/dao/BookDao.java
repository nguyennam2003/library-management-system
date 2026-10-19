package com.ptit.library.dao;

import com.ptit.library.model.BookItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class BookDao {

    public int countSearch(
            Connection connection,
            String keyword,
            Long categoryId,
            boolean onlyAvailable,
            boolean onlyActiveBooks) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM books b JOIN categories c ON c.id = b.category_id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, keyword, categoryId, onlyAvailable, onlyActiveBooks);

        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bindParams(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public List<BookItem> search(
            Connection connection,
            String keyword,
            Long categoryId,
            boolean onlyAvailable,
            boolean onlyActiveBooks,
            int page,
            int pageSize) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT b.id, b.title, b.isbn, b.category_id, c.name AS category_name, "
                        + "b.publisher, b.publish_year, b.status, "
                        + "(SELECT COUNT(*) FROM book_copies bc WHERE bc.book_id = b.id) AS total_copies, "
                        + "CASE WHEN b.status = 'ACTIVE' THEN "
                        + "(SELECT COUNT(*) FROM book_copies bc WHERE bc.book_id = b.id AND bc.status = 'AVAILABLE') "
                        + "ELSE 0 END AS available_copies, "
                        + "EXISTS (SELECT 1 FROM book_copies bc JOIN loan_details ld ON ld.copy_id = bc.id "
                        + "WHERE bc.book_id = b.id) AS has_loan_history "
                        + "FROM books b JOIN categories c ON c.id = b.category_id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, keyword, categoryId, onlyAvailable, onlyActiveBooks);
        sql.append(" ORDER BY b.id DESC LIMIT ? OFFSET ?");
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, pageSize);
        params.add(safeSize);
        params.add((safePage - 1) * safeSize);

        List<BookItem> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bindParams(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(connection, rs));
                }
            }
        }
        return result;
    }

    public Optional<BookItem> findById(Connection connection, long id) throws SQLException {
        String sql = "SELECT b.id, b.title, b.isbn, b.category_id, c.name AS category_name, "
                + "b.publisher, b.publish_year, b.status, "
                + "(SELECT COUNT(*) FROM book_copies bc WHERE bc.book_id = b.id) AS total_copies, "
                + "CASE WHEN b.status = 'ACTIVE' THEN "
                + "(SELECT COUNT(*) FROM book_copies bc WHERE bc.book_id = b.id AND bc.status = 'AVAILABLE') "
                + "ELSE 0 END AS available_copies, "
                + "EXISTS (SELECT 1 FROM book_copies bc JOIN loan_details ld ON ld.copy_id = bc.id "
                + "WHERE bc.book_id = b.id) AS has_loan_history "
                + "FROM books b JOIN categories c ON c.id = b.category_id WHERE b.id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(connection, rs));
            }
        }
    }

    public boolean existsByIsbn(Connection connection, String isbn, Long excludeBookId) throws SQLException {
        if (isbn == null || isbn.isBlank()) {
            return false;
        }
        String sql = excludeBookId == null
                ? "SELECT 1 FROM books WHERE isbn = ?"
                : "SELECT 1 FROM books WHERE isbn = ? AND id <> ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, isbn);
            if (excludeBookId != null) {
                statement.setLong(2, excludeBookId);
            }
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean hasLoanHistory(Connection connection, long bookId) throws SQLException {
        String sql = "SELECT 1 FROM book_copies bc JOIN loan_details ld ON ld.copy_id = bc.id "
                + "WHERE bc.book_id = ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, bookId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean hasBorrowedCopy(Connection connection, long bookId) throws SQLException {
        String sql = "SELECT 1 FROM book_copies WHERE book_id = ? AND status = 'BORROWED' LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, bookId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    public long insert(
            Connection connection,
            String title,
            String isbn,
            long categoryId,
            String publisher,
            int publishYear) throws SQLException {
        String sql = "INSERT INTO books (title, isbn, category_id, publisher, publish_year, status) "
                + "VALUES (?, ?, ?, ?, ?, 'ACTIVE')";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, title);
            if (isbn == null || isbn.isBlank()) {
                statement.setNull(2, Types.VARCHAR);
            } else {
                statement.setString(2, isbn);
            }
            statement.setLong(3, categoryId);
            statement.setString(4, publisher);
            statement.setInt(5, publishYear);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("book insert returned no id");
                }
                return keys.getLong(1);
            }
        }
    }

    public void update(
            Connection connection,
            long id,
            String title,
            String isbn,
            long categoryId,
            String publisher,
            int publishYear) throws SQLException {
        String sql = "UPDATE books SET title = ?, isbn = ?, category_id = ?, publisher = ?, publish_year = ? "
                + "WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            if (isbn == null || isbn.isBlank()) {
                statement.setNull(2, Types.VARCHAR);
            } else {
                statement.setString(2, isbn);
            }
            statement.setLong(3, categoryId);
            statement.setString(4, publisher);
            statement.setInt(5, publishYear);
            statement.setLong(6, id);
            statement.executeUpdate();
        }
    }

    public void replaceBookAuthors(Connection connection, long bookId, List<Long> authorIds) throws SQLException {
        try (PreparedStatement deleteStmt = connection.prepareStatement(
                "DELETE FROM book_authors WHERE book_id = ?")) {
            deleteStmt.setLong(1, bookId);
            deleteStmt.executeUpdate();
        }
        String insertSql = "INSERT INTO book_authors (book_id, author_id) VALUES (?, ?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            for (Long authorId : authorIds) {
                insertStmt.setLong(1, bookId);
                insertStmt.setLong(2, authorId);
                insertStmt.addBatch();
            }
            insertStmt.executeBatch();
        }
    }

    public void updateStatus(Connection connection, long bookId, String status) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE books SET status = ? WHERE id = ?")) {
            statement.setString(1, status);
            statement.setLong(2, bookId);
            statement.executeUpdate();
        }
    }

    public void delete(Connection connection, long bookId) throws SQLException {
        try (PreparedStatement deleteAuthors = connection.prepareStatement(
                "DELETE FROM book_authors WHERE book_id = ?")) {
            deleteAuthors.setLong(1, bookId);
            deleteAuthors.executeUpdate();
        }
        try (PreparedStatement deleteCopies = connection.prepareStatement(
                "DELETE FROM book_copies WHERE book_id = ?")) {
            deleteCopies.setLong(1, bookId);
            deleteCopies.executeUpdate();
        }
        try (PreparedStatement deleteBook = connection.prepareStatement(
                "DELETE FROM books WHERE id = ?")) {
            deleteBook.setLong(1, bookId);
            deleteBook.executeUpdate();
        }
    }

    private static void appendFilters(
            StringBuilder sql,
            List<Object> params,
            String keyword,
            Long categoryId,
            boolean onlyAvailable,
            boolean onlyActiveBooks) {
        if (onlyActiveBooks) {
            sql.append(" AND b.status = 'ACTIVE'");
        }
        if (categoryId != null && categoryId > 0) {
            sql.append(" AND b.category_id = ?");
            params.add(categoryId);
        }
        if (onlyAvailable) {
            sql.append(" AND b.status = 'ACTIVE' AND EXISTS ("
                    + "SELECT 1 FROM book_copies bc WHERE bc.book_id = b.id AND bc.status = 'AVAILABLE')");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String trimmed = keyword.trim();
            String like = "%" + trimmed + "%";
            String isbnLike = "%" + trimmed.replace("-", "").replace(" ", "") + "%";
            sql.append(" AND (b.title LIKE ? OR b.isbn LIKE ? OR EXISTS ("
                    + "SELECT 1 FROM book_authors ba JOIN authors a ON a.id = ba.author_id "
                    + "WHERE ba.book_id = b.id AND a.name LIKE ?))");
            params.add(like);
            params.add(isbnLike);
            params.add(like);
        }
    }

    private static void bindParams(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object val = params.get(i);
            if (val instanceof Long l) {
                statement.setLong(i + 1, l);
            } else if (val instanceof Integer n) {
                statement.setInt(i + 1, n);
            } else {
                statement.setString(i + 1, String.valueOf(val));
            }
        }
    }

    private static BookItem mapRow(Connection connection, ResultSet rs) throws SQLException {
        long bookId = rs.getLong("id");
        List<Long> authorIds = new ArrayList<>();
        List<String> authorNames = new ArrayList<>();
        String authorSql = "SELECT a.id, a.name FROM book_authors ba "
                + "JOIN authors a ON a.id = ba.author_id WHERE ba.book_id = ? ORDER BY a.name ASC";
        try (PreparedStatement authorStmt = connection.prepareStatement(authorSql)) {
            authorStmt.setLong(1, bookId);
            try (ResultSet ars = authorStmt.executeQuery()) {
                while (ars.next()) {
                    authorIds.add(ars.getLong("id"));
                    authorNames.add(ars.getString("name"));
                }
            }
        }
        return new BookItem(
                bookId,
                rs.getString("title"),
                rs.getString("isbn"),
                rs.getLong("category_id"),
                rs.getString("category_name"),
                rs.getString("publisher"),
                rs.getInt("publish_year"),
                rs.getString("status"),
                authorIds,
                String.join(", ", authorNames),
                rs.getInt("total_copies"),
                rs.getInt("available_copies"),
                rs.getBoolean("has_loan_history"));
    }
}
