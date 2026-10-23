package com.ptit.library.service;

import com.ptit.library.auth.PasswordHasher;
import com.ptit.library.auth.RegistrationInput;
import com.ptit.library.auth.RegistrationValidator;
import com.ptit.library.dao.ConnectionFactory;
import com.ptit.library.dao.ReaderDao;
import com.ptit.library.dao.UserDao;
import com.ptit.library.model.ReaderProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Nghiệp vụ quản lý độc giả của thủ thư/admin
public class StaffReaderService {

    public static final String ATTR_NAME = "staffReaderService";

    private final ConnectionFactory connections;
    private final UserDao users = new UserDao();
    private final ReaderDao readers = new ReaderDao();

    public StaffReaderService(ConnectionFactory connections) {
        this.connections = connections;
    }

    private static ReaderProfile map(ResultSet rs) throws SQLException {
        return new ReaderProfile(rs.getLong("id"), rs.getLong("user_id"), rs.getString("code"),
                rs.getString("full_name"), rs.getString("email"), rs.getString("phone"), rs.getString("status"));
    }

    // tìm theo mã hoặc họ tên; status rỗng = tất cả
    public List<ReaderProfile> search(String keyword, String status) {
        List<ReaderProfile> list = new ArrayList<>();
        String sql = "SELECT id, user_id, code, full_name, email, phone, status FROM readers "
                + "WHERE (code LIKE ? OR full_name LIKE ?) AND (? = '' OR status = ?) ORDER BY id DESC";
        String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        String st = status == null ? "" : status;
        try (Connection c = connections.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, st);
            ps.setString(4, st);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tìm độc giả", e);
        }
        return list;
    }

    // hàng đợi duyệt: PENDING và REJECTED (để duyệt lại)
    public List<ReaderProfile> listQueue() {
        List<ReaderProfile> list = new ArrayList<>();
        String sql = "SELECT id, user_id, code, full_name, email, phone, status FROM readers "
                + "WHERE status IN ('PENDING', 'REJECTED') ORDER BY FIELD(status, 'PENDING', 'REJECTED'), id";
        try (Connection c = connections.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi lấy hàng đợi", e);
        }
        return list;
    }

    public Optional<ReaderProfile> findById(long id) {
        String sql = "SELECT id, user_id, code, full_name, email, phone, status FROM readers WHERE id = ?";
        try (Connection c = connections.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tìm độc giả", e);
        }
    }

    // tạo độc giả tại quầy, trạng thái ACTIVE. Trả về null nếu ok, ngược lại là lỗi
    public String createAtCounter(String username, String password, String fullName, String email, String phone) {
        RegistrationInput input = new RegistrationInput(username, password, password, fullName, email, phone);
        List<String> errors = new RegistrationValidator().validate(input);
        if (!errors.isEmpty()) {
            return errors.get(0);
        }
        try (Connection c = connections.getConnection()) {
            c.setAutoCommit(false);
            try {
                if (users.existsUsername(c, username.trim())) {
                    c.rollback();
                    return "Tên đăng nhập đã được sử dụng.";
                }
                if (readers.existsEmail(c, email.trim())) {
                    c.rollback();
                    return "Email đã được sử dụng.";
                }
                int roleId = users.findRoleId(c, "READER");
                long userId = users.insert(c, username.trim(), PasswordHasher.hash(password), roleId);
                String code = readers.nextCode(c);
                readers.insert(c, userId, code, fullName.trim(), email.trim(),
                        phone.trim().replaceAll("\\s+", ""), "ACTIVE");
                c.commit();
                return null;
            } catch (SQLException e) {
                c.rollback();
                return "Không tạo được độc giả.";
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return "Không tạo được độc giả.";
        }
    }

    // sửa họ tên, email, sđt (không đổi username)
    public String update(long id, String fullName, String email, String phone) {
        fullName = fullName == null ? "" : fullName.trim();
        email = email == null ? "" : email.trim();
        phone = phone == null ? "" : phone.trim().replaceAll("\\s+", "");
        if (fullName.isEmpty()) {
            return "Họ tên không được để trống.";
        }
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "Email không hợp lệ.";
        }
        if (!phone.matches("^0?\\d{9,14}$")) {
            return "Số điện thoại không hợp lệ.";
        }
        try (Connection c = connections.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM readers WHERE email = ? AND id <> ?")) {
                ps.setString(1, email);
                ps.setLong(2, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return "Email đã được sử dụng.";
                    }
                }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE readers SET full_name = ?, email = ?, phone = ? WHERE id = ?")) {
                ps.setString(1, fullName);
                ps.setString(2, email);
                ps.setString(3, phone);
                ps.setLong(4, id);
                if (ps.executeUpdate() == 0) {
                    return "Không tìm thấy độc giả.";
                }
            }
            return null;
        } catch (SQLException e) {
            return "Không cập nhật được độc giả.";
        }
    }

    // đổi trạng thái thẻ theo action: approve, reject, reopen, lock, unlock
    public String changeStatus(long id, String action) {
        String from;
        String to;
        switch (action == null ? "" : action) {
            case "approve" -> { from = "PENDING,REJECTED"; to = "ACTIVE"; }
            case "reject" -> { from = "PENDING"; to = "REJECTED"; }
            case "reopen" -> { from = "REJECTED"; to = "PENDING"; }
            case "lock" -> { from = "ACTIVE"; to = "LOCKED"; }
            case "unlock" -> { from = "LOCKED"; to = "ACTIVE"; }
            default -> { return "Thao tác không hợp lệ."; }
        }
        Optional<ReaderProfile> reader = findById(id);
        if (reader.isEmpty()) {
            return "Không tìm thấy độc giả.";
        }
        if (!List.of(from.split(",")).contains(reader.get().status())) {
            return "Không thể chuyển từ trạng thái " + reader.get().status() + " sang " + to + ".";
        }
        try (Connection c = connections.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE readers SET status = ? WHERE id = ?")) {
            ps.setString(1, to);
            ps.setLong(2, id);
            ps.executeUpdate();
            return null;
        } catch (SQLException e) {
            return "Không đổi được trạng thái.";
        }
    }
}
