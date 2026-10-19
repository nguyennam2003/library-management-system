package com.ptit.library.dao;

import com.ptit.library.model.UserAccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public final class UserDao {

    public Optional<UserAccount> findByUsername(Connection connection, String username) throws SQLException {
        String sql = "SELECT u.id, u.username, u.password_hash, u.role_id, r.code, u.locked "
                + "FROM users u JOIN roles r ON r.id = u.role_id WHERE u.username = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(rs));
            }
        }
    }

    public boolean existsUsername(Connection connection, String username) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM users WHERE username = ?")) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int findRoleId(Connection connection, String roleCode) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM roles WHERE code = ?")) {
            statement.setString(1, roleCode);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("missing role: " + roleCode);
                }
                return rs.getInt(1);
            }
        }
    }

    public long insert(Connection connection, String username, String passwordHash, int roleId) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role_id, locked) VALUES (?, ?, ?, 0)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setInt(3, roleId);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("user insert returned no id");
                }
                return keys.getLong(1);
            }
        }
    }

    public Optional<UserAccount> findById(Connection connection, long id) throws SQLException {
        String sql = "SELECT u.id, u.username, u.password_hash, u.role_id, r.code, u.locked "
                + "FROM users u JOIN roles r ON r.id = u.role_id WHERE u.id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(rs));
            }
        }
    }

    public void updatePasswordHash(Connection connection, long userId, String passwordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passwordHash);
            statement.setLong(2, userId);
            statement.executeUpdate();
        }
    }

    private static UserAccount map(ResultSet rs) throws SQLException {
        return new UserAccount(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getLong("role_id"),
                rs.getString("code"),
                rs.getBoolean("locked"));
    }
}
