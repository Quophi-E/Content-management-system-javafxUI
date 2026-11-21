package com.educms.dao;

import org.mindrot.jbcrypt.BCrypt;

import java.io.*;
import java.nio.file.*;
import java.sql.*;

/**
 * Central place for obtaining JDBC connections and bootstrapping the schema.
 *
 * Uses an embedded SQLite database file (edu_cms.db) so the project runs with
 * zero external setup -- ideal for classroom/demo use. To point this at a
 * MySQL server instead, swap DB_URL for a jdbc:mysql:// URL, add the
 * mysql-connector-j dependency in pom.xml, and adjust the AUTOINCREMENT /
 * datetime() syntax in schema.sql to MySQL equivalents (AUTO_INCREMENT, NOW()).
 */
public class DatabaseManager {

    private static final String DB_FILE = "edu_cms.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    private static Connection connection;

    private DatabaseManager() {}

    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
                try (Statement st = connection.createStatement()) {
                    st.execute("PRAGMA foreign_keys = ON;");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to database", e);
        }
        return connection;
    }

    /** Creates tables if they don't exist and seeds a default admin account. */
    public static void initializeDatabase() {
        try (InputStream in = DatabaseManager.class.getResourceAsStream("/com/educms/db/schema.sql")) {
            if (in == null) throw new FileNotFoundException("schema.sql not found on classpath");
            String schema = new String(in.readAllBytes());
// Strip full-line SQL comments before splitting on ';'
String cleaned = schema.lines()
        .filter(line -> !line.trim().startsWith("--"))
        .reduce("", (a, b) -> a + "\n" + b);
Connection conn = getConnection();
try (Statement st = conn.createStatement()) {
    for (String stmt : cleaned.split(";")) {
        String trimmed = stmt.trim();
        if (!trimmed.isEmpty()) {
            st.execute(trimmed);
                    }
                }
            }
            seedDefaultAdmin(conn);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private static void seedDefaultAdmin(Connection conn) throws SQLException {
        try (PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM users")) {
            ResultSet rs = check.executeQuery();
            rs.next();
            if (rs.getInt(1) == 0) {
                String hash = BCrypt.hashpw("Admin@123", BCrypt.gensalt());
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO users (username, password_hash, full_name, email, role) VALUES (?,?,?,?,?)")) {
                    ins.setString(1, "admin");
                    ins.setString(2, hash);
                    ins.setString(3, "System Administrator");
                    ins.setString(4, "admin@educms.local");
                    ins.setString(5, "ADMIN");
                    ins.executeUpdate();
                }
            }
        }
    }

    /** Directory where uploaded media files are physically copied to. */
    public static Path getMediaStoreDir() {
        Path dir = Paths.get("media_store");
        try {
            if (!Files.exists(dir)) Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create media_store directory", e);
        }
        return dir;
    }
}
