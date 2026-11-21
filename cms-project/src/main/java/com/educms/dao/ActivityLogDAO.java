package com.educms.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDAO {

    public record Entry(String userName, String action, String details, LocalDateTime timestamp) {}

    public void log(int userId, String action, String details) {
        String sql = "INSERT INTO activity_log (user_id, action, details) VALUES (?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, action);
            ps.setString(3, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Entry> recent(int limit) {
        List<Entry> list = new ArrayList<>();
        String sql = "SELECT a.action, a.details, a.timestamp, u.full_name FROM activity_log a " +
                "LEFT JOIN users u ON a.user_id = u.id ORDER BY a.timestamp DESC LIMIT ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String ts = rs.getString("timestamp");
                list.add(new Entry(
                        rs.getString("full_name"),
                        rs.getString("action"),
                        rs.getString("details"),
                        ts != null ? LocalDateTime.parse(ts.replace(' ', 'T')) : null
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
}
