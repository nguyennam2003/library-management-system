package com.ptit.library.dao;

import com.ptit.library.model.Author;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AuthorDao {

    public List<Author> findAll(Connection connection) throws SQLException {
        String sql = "SELECT a.id, a.name, COUNT(ba.book_id) AS book_count "
                + "FROM authors a LEFT JOIN book_authors ba ON ba.author_id = a.id "
                + "GROUP BY a.id, a.name ORDER BY a.name ASC";
        List<Author> list = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                list.add(new Author(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getInt("book_count")));
            }
        }
        return list;
    }

    public Optional<Author> findById(Connection connection, long id) throws SQLException {
        String sql = "SELECT a.id, a.name, COUNT(ba.book_id) AS book_count "
                + "FROM authors a LEFT JOIN book_authors ba ON ba.author_id = a.id "
                + "WHERE a.id = ? GROUP BY a.id, a.name";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Author(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getInt("book_count")));
            }
        }
    }

    public int countBooks(Connection connection, long authorId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM book_authors WHERE author_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, authorId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public long insert(Connection connection, String name) throws SQLException {
        String sql = "INSERT INTO authors (name) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("author insert returned no id");
                }
                return keys.getLong(1);
            }
        }
    }

    public void update(Connection connection, long id, String name) throws SQLException {
        String sql = "UPDATE authors SET name = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setLong(2, id);
            statement.executeUpdate();
        }
    }

    public void delete(Connection connection, long id) throws SQLException {
        String sql = "DELETE FROM authors WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }
}
