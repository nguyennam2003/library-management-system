package com.ptit.library.service;

import com.ptit.library.auth.CredentialVerifier;
import com.ptit.library.auth.PasswordHasher;
import com.ptit.library.dao.ConnectionFactory;
import com.ptit.library.dao.ReaderDao;
import com.ptit.library.dao.UserDao;
import com.ptit.library.model.ReaderProfile;
import com.ptit.library.model.UserAccount;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public final class AuthService {

    private final ConnectionFactory connections;
    private final UserDao users = new UserDao();
    private final ReaderDao readers = new ReaderDao();
    private final CredentialVerifier verifier = new CredentialVerifier();

    public AuthService(ConnectionFactory connections) {
        this.connections = connections;
    }

    public CredentialVerifier.Result login(String username, String password) {
        try (Connection connection = connections.getConnection()) {
            Optional<UserAccount> user = users.findByUsername(connection, username == null ? "" : username.trim());
            ReaderProfile reader = null;
            if (user.isPresent() && "READER".equals(user.get().roleCode())) {
                reader = readers.findByUserId(connection, user.get().id()).orElse(null);
            }
            return verifier.verify(user.orElse(null), reader, password);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đăng nhập", e);
        }
    }

    public String changePassword(
            long userId,
            String currentPassword,
            String newPassword,
            String confirmPassword) {
        try (Connection connection = connections.getConnection()) {
            Optional<UserAccount> userOpt = users.findById(connection, userId);
            String validationError = validatePasswordChange(
                    userOpt.orElse(null), currentPassword, newPassword, confirmPassword);
            if (validationError != null) {
                return validationError;
            }
            users.updatePasswordHash(connection, userId, PasswordHasher.hash(newPassword));
            return null;
        } catch (SQLException e) {
            return "Không thể đổi mật khẩu. Vui lòng thử lại.";
        }
    }

    public static String validatePasswordChange(
            UserAccount user,
            String currentPassword,
            String newPassword,
            String confirmPassword) {
        if (user == null) {
            return "Không tìm thấy tài khoản.";
        }
        String current = currentPassword == null ? "" : currentPassword;
        String next = newPassword == null ? "" : newPassword;
        String confirm = confirmPassword == null ? "" : confirmPassword;

        if (current.isEmpty() || !PasswordHasher.check(current, user.passwordHash())) {
            return "Mật khẩu hiện tại không đúng.";
        }
        if (next.length() < 8) {
            return "Mật khẩu mới phải có ít nhất 8 ký tự.";
        }
        if (!next.equals(confirm)) {
            return "Xác nhận mật khẩu mới không khớp.";
        }
        return null;
    }
}
