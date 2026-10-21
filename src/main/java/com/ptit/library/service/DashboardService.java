package com.ptit.library.service;

import com.ptit.library.dao.ConnectionFactory;
import com.ptit.library.model.TopBook;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

// Thống kê cho dashboard
public class DashboardService {

    public static final String ATTR_NAME = "dashboardService";

    private final ConnectionFactory connections;

    public DashboardService(ConnectionFactory connections) {
        this.connections = connections;
    }

    private static int count(Connection c, String sql) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public int totalBooks() {
        return one("SELECT COUNT(*) FROM books");
    }

    public int totalCopies() {
        return one("SELECT COUNT(*) FROM book_copies");
    }

    public int activeReaders() {
        return one("SELECT COUNT(*) FROM readers WHERE status = 'ACTIVE'");
    }

    public int borrowedCopies() {
        return one("SELECT COUNT(*) FROM book_copies WHERE status = 'BORROWED'");
    }

    public int overdueCount() {
        return one("SELECT COUNT(*) FROM loan_details WHERE status = 'BORROWED' AND due_date < CURDATE()");
    }

    private int one(String sql) {
        try (Connection c = connections.getConnection()) {
            return count(c, sql);
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi thống kê", e);
        }
    }

    // top đầu sách mượn nhiều nhất, có thể lọc theo ngày mượn (from/to có thể null)
    public List<TopBook> topBooks(LocalDate from, LocalDate to) {
        String sql = "SELECT b.title, COUNT(*) AS cnt FROM loan_details ld "
                + "JOIN loans l ON l.id = ld.loan_id "
                + "JOIN book_copies bc ON bc.id = ld.copy_id "
                + "JOIN books b ON b.id = bc.book_id "
                + "WHERE (? IS NULL OR l.borrowed_on >= ?) AND (? IS NULL OR l.borrowed_on <= ?) "
                + "GROUP BY b.id, b.title ORDER BY cnt DESC, b.title LIMIT 10";
        List<TopBook> list = new ArrayList<>();
        try (Connection c = connections.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            Date f = from == null ? null : Date.valueOf(from);
            Date t = to == null ? null : Date.valueOf(to);
            ps.setDate(1, f);
            ps.setDate(2, f);
            ps.setDate(3, t);
            ps.setDate(4, t);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TopBook(rs.getString("title"), rs.getInt("cnt")));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi thống kê top sách", e);
        }
        return list;
    }

    // số lượt mượn (số cuốn) trong khoảng ngày
    public int borrowCountInRange(LocalDate from, LocalDate to) {
        String sql = "SELECT COUNT(*) FROM loan_details ld JOIN loans l ON l.id = ld.loan_id "
                + "WHERE (? IS NULL OR l.borrowed_on >= ?) AND (? IS NULL OR l.borrowed_on <= ?)";
        try (Connection c = connections.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            Date f = from == null ? null : Date.valueOf(from);
            Date t = to == null ? null : Date.valueOf(to);
            ps.setDate(1, f);
            ps.setDate(2, f);
            ps.setDate(3, t);
            ps.setDate(4, t);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi thống kê", e);
        }
    }
}
