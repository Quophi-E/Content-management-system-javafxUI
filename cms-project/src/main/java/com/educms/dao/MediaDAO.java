package com.educms.dao;

import com.educms.model.MediaFile;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MediaDAO {

    private static final String JOIN_SELECT =
            "SELECT m.*, u.full_name AS uploader_name FROM media_files m " +
            "JOIN users u ON m.uploaded_by = u.id ";

    public List<MediaFile> findAll() {
        List<MediaFile> list = new ArrayList<>();
        String sql = JOIN_SELECT + "ORDER BY m.uploaded_at DESC";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<MediaFile> search(String keyword) {
        List<MediaFile> list = new ArrayList<>();
        String sql = JOIN_SELECT + "WHERE m.filename LIKE ? OR m.description LIKE ? ORDER BY m.uploaded_at DESC";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM media_files";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long totalStorageBytes() {
        String sql = "SELECT COALESCE(SUM(file_size),0) FROM media_files";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insert(MediaFile m) {
        String sql = "INSERT INTO media_files (filename, filepath, file_type, file_size, description, " +
                "linked_content_id, uploaded_by) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, m.getFilename());
            ps.setString(2, m.getFilepath());
            ps.setString(3, m.getFileType());
            ps.setLong(4, m.getFileSize());
            ps.setString(5, m.getDescription());
            if (m.getLinkedContentId() != null) ps.setInt(6, m.getLinkedContentId());
            else ps.setNull(6, Types.INTEGER);
            ps.setInt(7, m.getUploadedBy());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM media_files WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private MediaFile mapRow(ResultSet rs) throws SQLException {
        MediaFile m = new MediaFile();
        m.setId(rs.getInt("id"));
        m.setFilename(rs.getString("filename"));
        m.setFilepath(rs.getString("filepath"));
        m.setFileType(rs.getString("file_type"));
        m.setFileSize(rs.getLong("file_size"));
        m.setDescription(rs.getString("description"));
        int linked = rs.getInt("linked_content_id");
        m.setLinkedContentId(rs.wasNull() ? null : linked);
        m.setUploadedBy(rs.getInt("uploaded_by"));
        m.setUploaderName(rs.getString("uploader_name"));
        String uploaded = rs.getString("uploaded_at");
        if (uploaded != null) m.setUploadedAt(LocalDateTime.parse(uploaded.replace(' ', 'T')));
        return m;
    }
}
