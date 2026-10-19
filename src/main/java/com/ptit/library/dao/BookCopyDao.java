package com.ptit.library.dao;

import com.ptit.library.model.BookCopy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class BookCopyDao {

    public List<BookCopy> findByBookId(Connection connection, long bookId) throws SQLException {
        String sql = "SELECT bc.id, bc.book_id, bc.code, bc.status, "
                + "EXISTS (SELECT 1 FROM loan_details ld WHERE ld.copy_id = bc.id) AS has_loan_history "
                + "FROM book_copies bc WHERE bc.book_id = ? ORDER BY bc.code ASC";
        List<BookCopy> list = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, bookId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public Optional<BookCopy> findById(Connection connection, long copyId) throws SQLException {
        String sql = "SELECT bc.id, bc.book_id, bc.code, bc.status, "
                + "EXISTS (SELECT 1 FROM loan_details ld WHERE ld.copy_id = bc.id) AS has_loan_history "
                + "FROM book_copies bc WHERE bc.id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, copyId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(rs));
            }
        }
    }

    public boolean existsByCode(Connection connection, String code) throws SQLException {
        String sql = "SELECT 1 FROM book_copies WHERE LOWER(code) = LOWER(?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    public long insert(Connection connection, long bookId, String code) throws SQLException {
        String sql = "INSERT INTO book_copies (book_id, code, status) VALUES (?, ?, 'AVAILABLE')";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, bookId);
            statement.setString(2, code);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("book_copies insert returned no id");
                }
                return keys.getLong(1);
            }
        }
    }

    public void updateStatus(Connection connection, long copyId, String status) throws SQLException {
        String sql = "UPDATE book_copies SET status = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setLong(2, copyId);
            statement.executeUpdate();
        }
    }

    public void delete(Connection connection, long copyId) throws SQLException {
        String sql = "DELETE FROM book_copies WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, copyId);
            statement.executeUpdate();
        }
    }

    private static BookCopy map(ResultSet rs) throws SQLException {
        return new BookCopy(
                rs.getLong("id"),
                rs.getLong("book_id"),
                rs.getString("code"),
                rs.getString("status"),
                rs.getBoolean("has_loan_history"));
    }
}
