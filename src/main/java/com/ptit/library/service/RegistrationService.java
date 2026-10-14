package com.ptit.library.service;

import com.ptit.library.auth.PasswordHasher;
import com.ptit.library.auth.RegistrationInput;
import com.ptit.library.auth.RegistrationValidator;
import com.ptit.library.dao.ConnectionFactory;
import com.ptit.library.dao.ReaderDao;
import com.ptit.library.dao.UserDao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class RegistrationService {

    private final ConnectionFactory connections;
    private final RegistrationValidator validator = new RegistrationValidator();
    private final UserDao users = new UserDao();
    private final ReaderDao readers = new ReaderDao();

    public RegistrationService(ConnectionFactory connections) {
        this.connections = connections;
    }

    public RegistrationResult register(RegistrationInput input) {
        List<String> errors = validator.validate(input);
        if (!errors.isEmpty()) {
            return RegistrationResult.invalid(errors);
        }
        String username = input.username().trim();
        String email = input.email().trim();
        String fullName = input.fullName().trim();
        String phone = input.phone().trim().replaceAll("\\s+", "");
        try (Connection connection = connections.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (users.existsUsername(connection, username)) {
                    connection.rollback();
                    return RegistrationResult.invalid(List.of("Tên đăng nhập đã được sử dụng."));
                }
                if (readers.existsEmail(connection, email)) {
                    connection.rollback();
                    return RegistrationResult.invalid(List.of("Email đã được sử dụng."));
                }
                int roleId = users.findRoleId(connection, "READER");
                long userId = users.insert(connection, username, PasswordHasher.hash(input.password()), roleId);
                String code = readers.nextCode(connection);
                readers.insert(connection, userId, code, fullName, email, phone, "PENDING");
                connection.commit();
                return RegistrationResult.ok(code);
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return RegistrationResult.invalid(List.of("Không thể đăng ký. Vui lòng thử lại."));
        }
    }
}
