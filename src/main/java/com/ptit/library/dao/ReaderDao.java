package com.ptit.library.dao;

import com.ptit.library.model.ReaderProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public final class ReaderDao {

    public Optional<ReaderProfile> findByUserId(Connection connection, long userId) throws SQLException {
        String sql = "SELECT id, user_id, code, full_name, email, phone, status FROM readers WHERE user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new ReaderProfile(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("code"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("status")));
            }
        }
    }

    public boolean existsEmail(Connection connection, String email) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM readers WHERE email = ?")) {
            statement.setString(1, email);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    public String nextCode(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT MAX(CAST(SUBSTRING(code, 3) AS UNSIGNED)) FROM readers");
             ResultSet rs = statement.executeQuery()) {
            long max = 0;
            if (rs.next()) {
                max = rs.getLong(1);
            }
            return ReaderCodes.formatReaderCode(max + 1);
        }
    }

    public void insert(
            Connection connection,
            long userId,
            String code,
            String fullName,
            String email,
            String phone,
            String status) throws SQLException {
        String sql = "INSERT INTO readers (user_id, code, full_name, email, phone, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, code);
            statement.setString(3, fullName);
            statement.setString(4, email);
            statement.setString(5, phone);
            statement.setString(6, status);
            statement.executeUpdate();
        }
    }
}
