package com.ptit.library.service;

import com.ptit.library.auth.PasswordHasher;
import com.ptit.library.dao.ConnectionFactory;
import com.ptit.library.dao.UserDao;
import com.ptit.library.model.UserAccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Admin quản lý tài khoản ADMIN / LIBRARIAN
public class StaffUserService {

    public static final String ATTR_NAME = "staffUserService";

    private final ConnectionFactory connections;
    private final UserDao users = new UserDao();

    public StaffUserService(ConnectionFactory connections) {
        this.connections = connections;
    }

    public List<UserAccount> list() {
        List<UserAccount> list = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.password_hash, u.role_id, r.code, u.locked "
                + "FROM users u JOIN roles r ON r.id = u.role_id WHERE r.code IN ('ADMIN', 'LIBRARIAN') ORDER BY u.id";
        try (Connection c = connections.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new UserAccount(rs.getLong("id"), rs.getString("username"), rs.getString("password_hash"),
                        rs.getLong("role_id"), rs.getString("code"), rs.getBoolean("locked")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi lấy danh sách nhân viên", e);
        }
        return list;
    }

    public Optional<UserAccount> findStaff(long id) {
        try (Connection c = connections.getConnection()) {
            Optional<UserAccount> u = users.findById(c, id);
            if (u.isPresent() && ("ADMIN".equals(u.get().roleCode()) || "LIBRARIAN".equals(u.get().roleCode()))) {
                return u;
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tìm tài khoản", e);
        }
    }

    public String create(String username, String password, String role) {
        username = username == null ? "" : username.trim();
        if (!username.matches("^[a-zA-Z0-9._]{4,32}$")) {
            return "Tên đăng nhập phải từ 4–32 ký tự, gồm chữ, số, dấu chấm hoặc gạch dưới.";
        }
        if (password == null || password.length() < 8) {
            return "Mật khẩu phải có ít nhất 8 ký tự.";
        }
        if (!"ADMIN".equals(role) && !"LIBRARIAN".equals(role)) {
            return "Vai trò không hợp lệ.";
        }
        try (Connection c = connections.getConnection()) {
            if (users.existsUsername(c, username)) {
                return "Tên đăng nhập đã tồn tại.";
            }
            users.insert(c, username, PasswordHasher.hash(password), users.findRoleId(c, role));
            return null;
        } catch (SQLException e) {
            return "Không tạo được tài khoản.";
        }
    }

    // sửa vai trò và (tùy chọn) đặt lại mật khẩu
    public String update(long id, String role, String newPassword, long currentUserId) {
        if (!"ADMIN".equals(role) && !"LIBRARIAN".equals(role)) {
            return "Vai trò không hợp lệ.";
        }
        boolean hasPassword = newPassword != null && !newPassword.isEmpty();
        if (hasPassword && newPassword.length() < 8) {
            return "Mật khẩu mới phải có ít nhất 8 ký tự.";
        }
        if (id == currentUserId && !"ADMIN".equals(role)) {
            return "Bạn không thể tự hạ quyền của chính mình.";
        }
        if (findStaff(id).isEmpty()) {
            return "Không tìm thấy tài khoản nhân viên.";
        }
        try (Connection c = connections.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("UPDATE users SET role_id = ? WHERE id = ?")) {
                ps.setInt(1, users.findRoleId(c, role));
                ps.setLong(2, id);
                ps.executeUpdate();
            }
            if (hasPassword) {
                users.updatePasswordHash(c, id, PasswordHasher.hash(newPassword));
            }
            return null;
        } catch (SQLException e) {
            return "Không cập nhật được tài khoản.";
        }
    }

    public String setLocked(long id, boolean locked, long currentUserId) {
        if (locked && id == currentUserId) {
            return "Bạn không thể tự khóa chính mình.";
        }
        if (findStaff(id).isEmpty()) {
            return "Không tìm thấy tài khoản nhân viên.";
        }
        try (Connection c = connections.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET locked = ? WHERE id = ?")) {
            ps.setBoolean(1, locked);
            ps.setLong(2, id);
            ps.executeUpdate();
            return null;
        } catch (SQLException e) {
            return "Không đổi được trạng thái tài khoản.";
        }
    }
}
