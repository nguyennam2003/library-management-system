package com.ptit.library.dao;

import com.ptit.library.model.ReaderLoanItem;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public final class ReaderLoanDao {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public List<ReaderLoanItem> findByReaderId(Connection connection, long readerId) throws SQLException {
        String sql = "SELECT ld.id AS detail_id, l.id AS loan_id, l.code AS loan_code, "
                + "l.status AS loan_status, l.borrowed_on, b.id AS book_id, b.title AS book_title, "
                + "bc.code AS copy_code, ld.due_date, ld.returned_at, ld.status AS detail_status "
                + "FROM loans l "
                + "JOIN loan_details ld ON ld.loan_id = l.id "
                + "JOIN book_copies bc ON bc.id = ld.copy_id "
                + "JOIN books b ON b.id = bc.book_id "
                + "WHERE l.reader_id = ? "
                + "ORDER BY l.borrowed_on DESC, ld.id DESC";
        LocalDate today = LocalDate.now(VN_ZONE);
        List<ReaderLoanItem> list = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, readerId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Date borrowedOnDate = rs.getDate("borrowed_on");
                    Date dueDateVal = rs.getDate("due_date");
                    Date returnedAtDate = rs.getDate("returned_at");
                    LocalDate borrowedOn = borrowedOnDate != null ? borrowedOnDate.toLocalDate() : null;
                    LocalDate dueDate = dueDateVal != null ? dueDateVal.toLocalDate() : null;
                    LocalDate returnedAt = returnedAtDate != null ? returnedAtDate.toLocalDate() : null;
                    String detailStatus = rs.getString("detail_status");
                    boolean overdue = "BORROWED".equals(detailStatus)
                            && dueDate != null
                            && today.isAfter(dueDate);

                    list.add(new ReaderLoanItem(
                            rs.getLong("detail_id"),
                            rs.getLong("loan_id"),
                            rs.getString("loan_code"),
                            rs.getString("loan_status"),
                            borrowedOn,
                            rs.getLong("book_id"),
                            rs.getString("book_title"),
                            rs.getString("copy_code"),
                            dueDate,
                            returnedAt,
                            detailStatus,
                            overdue));
                }
            }
        }
        return list;
    }

    public boolean belongsToReader(Connection connection, long loanId, long readerId) throws SQLException {
        String sql = "SELECT 1 FROM loans WHERE id = ? AND reader_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, loanId);
            statement.setLong(2, readerId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }
}
