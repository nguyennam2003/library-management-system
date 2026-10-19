package com.ptit.library.dao;

import com.ptit.library.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class CategoryDao {

    public List<Category> findAll(Connection connection) throws SQLException {
        String sql = "SELECT c.id, c.name, COUNT(b.id) AS book_count "
                + "FROM categories c LEFT JOIN books b ON b.category_id = c.id "
                + "GROUP BY c.id, c.name ORDER BY c.name ASC";
        List<Category> list = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                list.add(new Category(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getInt("book_count")));
            }
        }
        return list;
    }

    public Optional<Category> findById(Connection connection, long id) throws SQLException {
        String sql = "SELECT c.id, c.name, COUNT(b.id) AS book_count "
                + "FROM categories c LEFT JOIN books b ON b.category_id = c.id "
                + "WHERE c.id = ? GROUP BY c.id, c.name";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Category(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getInt("book_count")));
            }
        }
    }

    public boolean existsByName(Connection connection, String name, Long excludeId) throws SQLException {
        String sql = excludeId == null
                ? "SELECT 1 FROM categories WHERE LOWER(name) = LOWER(?)"
                : "SELECT 1 FROM categories WHERE LOWER(name) = LOWER(?) AND id <> ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            if (excludeId != null) {
                statement.setLong(2, excludeId);
            }
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int countBooks(Connection connection, long categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM books WHERE category_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, categoryId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public long insert(Connection connection, String name) throws SQLException {
        String sql = "INSERT INTO categories (name) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("category insert returned no id");
                }
                return keys.getLong(1);
            }
        }
    }

    public void update(Connection connection, long id, String name) throws SQLException {
        String sql = "UPDATE categories SET name = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setLong(2, id);
            statement.executeUpdate();
        }
    }

    public void delete(Connection connection, long id) throws SQLException {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }
}
