package com.ptit.library.service;

import com.ptit.library.dao.ConnectionFactory;
import com.ptit.library.dao.ReaderDao;
import com.ptit.library.dao.ReaderLoanDao;
import com.ptit.library.model.ReaderLoanItem;
import com.ptit.library.model.ReaderProfile;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class ReaderSelfService {

    public static final String ATTR_NAME = "readerSelfService";

    private final ConnectionFactory connections;
    private final ReaderDao readerDao = new ReaderDao();
    private final ReaderLoanDao readerLoanDao = new ReaderLoanDao();

    public ReaderSelfService(ConnectionFactory connections) {
        this.connections = connections;
    }

    public Optional<ReaderProfile> findProfileByUserId(long userId) {
        try (Connection conn = connections.getConnection()) {
            return readerDao.findByUserId(conn, userId);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải hồ sơ độc giả", e);
        }
    }

    public List<ReaderLoanItem> listAllLoans(long readerId) {
        try (Connection conn = connections.getConnection()) {
            return readerLoanDao.findByReaderId(conn, readerId);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải lịch sử mượn trả", e);
        }
    }

    public boolean verifyLoanOwnership(long loanId, long readerId) {
        try (Connection conn = connections.getConnection()) {
            return readerLoanDao.belongsToReader(conn, loanId, readerId);
        } catch (SQLException e) {
            return false;
        }
    }
}
