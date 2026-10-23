package com.ptit.library.service;

import com.ptit.library.dao.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingService {

    public static final String ATTR_NAME = "settingService";

    private final ConnectionFactory connections;

    public SettingService(ConnectionFactory connections) {
        this.connections = connections;
    }

    // lấy giá trị cấu hình, không có thì trả về giá trị mặc định
    public static int get(Connection c, String key, int defaultValue) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT setting_value FROM system_settings WHERE setting_key = ?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return defaultValue;
            }
        }
    }

    public int getMaxBorrowCount() {
        return getOne("max_borrow_count", 5);
    }

    public int getLoanPeriodDays() {
        return getOne("loan_period_days", 14);
    }

    private int getOne(String key, int def) {
        try (Connection c = connections.getConnection()) {
            return get(c, key, def);
        } catch (SQLException e) {
            return def;
        }
    }

    // trả về null nếu ok, ngược lại trả về câu báo lỗi
    public String save(String maxBorrow, String loanDays) {
        int max;
        int days;
        try {
            max = Integer.parseInt(maxBorrow.trim());
            days = Integer.parseInt(loanDays.trim());
        } catch (Exception e) {
            return "Giá trị phải là số nguyên.";
        }
        if (max < 1 || days < 1) {
            return "Giá trị phải lớn hơn hoặc bằng 1.";
        }
        try (Connection c = connections.getConnection()) {
            saveOne(c, "max_borrow_count", max);
            saveOne(c, "loan_period_days", days);
            return null;
        } catch (SQLException e) {
            return "Không lưu được cấu hình.";
        }
    }

    private void saveOne(Connection c, String key, int value) throws SQLException {
        String sql = "INSERT INTO system_settings (setting_key, setting_value) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setInt(2, value);
            ps.executeUpdate();
        }
    }
}
