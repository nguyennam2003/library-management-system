package com.ptit.library.service;

import com.ptit.library.auth.CredentialVerifier;
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
}
