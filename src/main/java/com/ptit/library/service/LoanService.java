package com.ptit.library.service;

import com.ptit.library.dao.ConnectionFactory;
import com.ptit.library.model.LoanLine;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Nghiệp vụ mượn / trả sách / quá hạn
public class LoanService {

    public static final String ATTR_NAME = "loanService";
    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final ConnectionFactory connections;

    public LoanService(ConnectionFactory connections) {
        this.connections = connections;
    }

    private static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    // Lập phiếu mượn. Thành công trả về "OK:<mã phiếu>", lỗi trả về "ERR:<nội dung>"
    public String createLoan(String readerCode, List<String> copyCodes, long staffUserId) {
        Set<String> codes = new HashSet<>();
        for (String s : copyCodes) {
            if (s != null && !s.trim().isEmpty()) {
                codes.add(s.trim());
            }
        }
        if (readerCode == null || readerCode.trim().isEmpty()) {
            return "ERR:Vui lòng nhập mã độc giả.";
        }
        if (codes.isEmpty()) {
            return "ERR:Vui lòng nhập ít nhất một mã bản sách.";
        }
        try (Connection c = connections.getConnection()) {
            c.setAutoCommit(false);
            try {
                // 1. kiểm tra độc giả
                long readerId;
                String status;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT id, status FROM readers WHERE code = ? FOR UPDATE")) {
                    ps.setString(1, readerCode.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            c.rollback();
                            return "ERR:Không tìm thấy độc giả có mã " + readerCode.trim() + ".";
                        }
                        readerId = rs.getLong("id");
                        status = rs.getString("status");
                    }
                }
                if (!"ACTIVE".equals(status)) {
                    c.rollback();
                    return "ERR:Thẻ độc giả chưa ở trạng thái ACTIVE (hiện là " + status + "), không được mượn.";
                }

                // 2. kiểm tra số lượng đang mượn
                int max = SettingService.get(c, "max_borrow_count", 5);
                int days = SettingService.get(c, "loan_period_days", 14);
                int borrowing = 0;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT COUNT(*) FROM loan_details ld JOIN loans l ON l.id = ld.loan_id "
                                + "WHERE l.reader_id = ? AND ld.status = 'BORROWED'")) {
                    ps.setLong(1, readerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            borrowing = rs.getInt(1);
                        }
                    }
                }
                if (borrowing + codes.size() > max) {
                    c.rollback();
                    return "ERR:Vượt quá số sách được mượn (tối đa " + max + ", đang mượn " + borrowing + ").";
                }

                // 3. kiểm tra từng bản sách
                List<Long> copyIds = new ArrayList<>();
                for (String code : codes) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "SELECT bc.id, bc.status, b.status AS book_status FROM book_copies bc "
                                    + "JOIN books b ON b.id = bc.book_id WHERE bc.code = ? FOR UPDATE")) {
                        ps.setString(1, code);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                c.rollback();
                                return "ERR:Không tìm thấy bản sách " + code + ".";
                            }
                            if (!"AVAILABLE".equals(rs.getString("status"))) {
                                c.rollback();
                                return "ERR:Bản sách " + code + " không sẵn sàng (" + rs.getString("status") + ").";
                            }
                            if (!"ACTIVE".equals(rs.getString("book_status"))) {
                                c.rollback();
                                return "ERR:Đầu sách của bản " + code + " đã ngừng lưu hành.";
                            }
                            copyIds.add(rs.getLong("id"));
                        }
                    }
                }

                // 4. tạo phiếu
                String loanCode;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT MAX(CAST(SUBSTRING(code, 3) AS UNSIGNED)) FROM loans");
                     ResultSet rs = ps.executeQuery()) {
                    long next = 1;
                    if (rs.next()) {
                        next = rs.getLong(1) + 1;
                    }
                    loanCode = String.format("PM%06d", next);
                }
                LocalDate borrowedOn = today();
                long loanId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO loans (code, reader_id, created_by_user_id, borrowed_on, status) "
                                + "VALUES (?, ?, ?, ?, 'OPEN')", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, loanCode);
                    ps.setLong(2, readerId);
                    ps.setLong(3, staffUserId);
                    ps.setDate(4, Date.valueOf(borrowedOn));
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        loanId = keys.getLong(1);
                    }
                }
                for (long copyId : copyIds) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO loan_details (loan_id, copy_id, due_date, status) VALUES (?, ?, ?, 'BORROWED')")) {
                        ps.setLong(1, loanId);
                        ps.setLong(2, copyId);
                        ps.setDate(3, Date.valueOf(borrowedOn.plusDays(days)));
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE book_copies SET status = 'BORROWED' WHERE id = ?")) {
                        ps.setLong(1, copyId);
                        ps.executeUpdate();
                    }
                }
                c.commit();
                return "OK:" + loanCode;
            } catch (SQLException e) {
                c.rollback();
                return "ERR:Không lập được phiếu mượn. Vui lòng thử lại.";
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return "ERR:Không kết nối được cơ sở dữ liệu.";
        }
    }

    // Tìm các dòng chi tiết theo mã phiếu / mã độc giả / họ tên.
    // onlyBorrowed = true thì chỉ lấy sách chưa trả; onlyOverdue = true thì chỉ lấy sách quá hạn
    public List<LoanLine> findLines(String keyword, boolean onlyBorrowed, boolean onlyOverdue) {
        String kw = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        StringBuilder sql = new StringBuilder(
                "SELECT ld.id, l.code AS loan_code, r.code AS reader_code, r.full_name, b.title, bc.code AS copy_code, "
                        + "l.borrowed_on, ld.due_date, ld.returned_at, ld.status "
                        + "FROM loan_details ld JOIN loans l ON l.id = ld.loan_id "
                        + "JOIN readers r ON r.id = l.reader_id "
                        + "JOIN book_copies bc ON bc.id = ld.copy_id "
                        + "JOIN books b ON b.id = bc.book_id "
                        + "WHERE (l.code LIKE ? OR r.code LIKE ? OR r.full_name LIKE ?) ");
        if (onlyBorrowed || onlyOverdue) {
            sql.append("AND ld.status = 'BORROWED' ");
        }
        if (onlyOverdue) {
            sql.append("AND ld.due_date < ? ");
        }
        sql.append("ORDER BY ld.due_date, ld.id DESC LIMIT 200");
        LocalDate today = today();
        List<LoanLine> list = new ArrayList<>();
        try (Connection c = connections.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);
            if (onlyOverdue) {
                ps.setDate(4, Date.valueOf(today));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate due = rs.getDate("due_date").toLocalDate();
                    Date ret = rs.getDate("returned_at");
                    String status = rs.getString("status");
                    long overdue = 0;
                    if ("BORROWED".equals(status) && due.isBefore(today)) {
                        overdue = ChronoUnit.DAYS.between(due, today);
                    }
                    list.add(new LoanLine(rs.getLong("id"), rs.getString("loan_code"), rs.getString("reader_code"),
                            rs.getString("full_name"), rs.getString("title"), rs.getString("copy_code"),
                            rs.getDate("borrowed_on").toLocalDate(), due,
                            ret == null ? null : ret.toLocalDate(), status, overdue));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tra cứu phiếu mượn", e);
        }
        return list;
    }

    // Xác nhận trả 1 cuốn. Trả về null nếu ok, ngược lại là lỗi
    public String returnCopy(long detailId) {
        try (Connection c = connections.getConnection()) {
            c.setAutoCommit(false);
            try {
                long loanId;
                long copyId;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT loan_id, copy_id, status FROM loan_details WHERE id = ? FOR UPDATE")) {
                    ps.setLong(1, detailId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            c.rollback();
                            return "Không tìm thấy dòng mượn sách.";
                        }
                        if (!"BORROWED".equals(rs.getString("status"))) {
                            c.rollback();
                            return "Cuốn sách này đã được trả rồi.";
                        }
                        loanId = rs.getLong("loan_id");
                        copyId = rs.getLong("copy_id");
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE loan_details SET status = 'RETURNED', returned_at = ? WHERE id = ?")) {
                    ps.setDate(1, Date.valueOf(today()));
                    ps.setLong(2, detailId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE book_copies SET status = 'AVAILABLE' WHERE id = ?")) {
                    ps.setLong(1, copyId);
                    ps.executeUpdate();
                }
                // nếu hết sách đang mượn thì phiếu COMPLETED
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT COUNT(*) FROM loan_details WHERE loan_id = ? AND status = 'BORROWED'")) {
                    ps.setLong(1, loanId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) == 0) {
                            try (PreparedStatement up = c.prepareStatement(
                                    "UPDATE loans SET status = 'COMPLETED' WHERE id = ?")) {
                                up.setLong(1, loanId);
                                up.executeUpdate();
                            }
                        }
                    }
                }
                c.commit();
                return null;
            } catch (SQLException e) {
                c.rollback();
                return "Không xác nhận được việc trả sách.";
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return "Không kết nối được cơ sở dữ liệu.";
        }
    }
}
