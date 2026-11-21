package com.educms.dao;

import com.educms.model.ContentItem;
import com.educms.model.ContentStatus;
import com.educms.model.ContentType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ContentDAO {

    private static final String JOIN_SELECT =
            "SELECT c.*, u.full_name AS author_name FROM content_items c " +
            "JOIN users u ON c.author_id = u.id ";

    public List<ContentItem> findByType(ContentType type) {
        List<ContentItem> list = new ArrayList<>();
        String sql = JOIN_SELECT + "WHERE c.type = ? ORDER BY c.updated_at DESC";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, type.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<ContentItem> search(ContentType type, String keyword) {
        List<ContentItem> list = new ArrayList<>();
        String sql = JOIN_SELECT + "WHERE c.type = ? AND (c.title LIKE ? OR c.tags LIKE ? OR c.category LIKE ?) " +
                "ORDER BY c.updated_at DESC";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, type.name());
            String like = "%" + keyword + "%";
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<ContentItem> findAll() {
        List<ContentItem> list = new ArrayList<>();
        String sql = JOIN_SELECT + "ORDER BY c.updated_at DESC";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public int countByType(ContentType type) {
        String sql = "SELECT COUNT(*) FROM content_items WHERE type = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, type.name());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int countByStatus(ContentStatus status) {
        String sql = "SELECT COUNT(*) FROM content_items WHERE status = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insert(ContentItem c) {
        String sql = "INSERT INTO content_items (type, title, body, category, tags, status, author_id) " +
                "VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getType().name());
            ps.setString(2, c.getTitle());
            ps.setString(3, c.getBody());
            ps.setString(4, c.getCategory());
            ps.setString(5, c.getTags());
            ps.setString(6, c.getStatus().name());
            ps.setInt(7, c.getAuthorId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(ContentItem c) {
        String sql = "UPDATE content_items SET title=?, body=?, category=?, tags=?, status=?, " +
                "updated_at=datetime('now') WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getTitle());
            ps.setString(2, c.getBody());
            ps.setString(3, c.getCategory());
            ps.setString(4, c.getTags());
            ps.setString(5, c.getStatus().name());
            ps.setInt(6, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM content_items WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ContentItem mapRow(ResultSet rs) throws SQLException {
        ContentItem c = new ContentItem();
        c.setId(rs.getInt("id"));
        c.setType(ContentType.valueOf(rs.getString("type")));
        c.setTitle(rs.getString("title"));
        c.setBody(rs.getString("body"));
        c.setCategory(rs.getString("category"));
        c.setTags(rs.getString("tags"));
        c.setStatus(ContentStatus.valueOf(rs.getString("status")));
        c.setAuthorId(rs.getInt("author_id"));
        c.setAuthorName(rs.getString("author_name"));
        String created = rs.getString("created_at");
        if (created != null) c.setCreatedAt(LocalDateTime.parse(created.replace(' ', 'T')));
        String updated = rs.getString("updated_at");
        if (updated != null) c.setUpdatedAt(LocalDateTime.parse(updated.replace(' ', 'T')));
        return c;
    }
}
