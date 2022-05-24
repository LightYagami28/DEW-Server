package me.pari;
import me.pari.security.Token;
import org.hydev.logger.HyLogger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

public class Storage {

    private static final HyLogger LOGGER = new HyLogger("Storage");

    private final String fileName;

    private static Storage INSTANCE;

    private Connection conn;

    public Storage(final String fileName) {
        this.fileName = fileName;
    }

    public static Storage getInstance() {
        if (INSTANCE == null)
            INSTANCE = new Storage(Server.DATABASE_NAME);
        return INSTANCE;
    }

    public synchronized void connect() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + System.getProperty("user.dir") + File.separator + fileName);
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public synchronized void setup() {
        if (conn == null)
            connect();

        try {
            String content = Files.readString(
                    Path.of(System.getProperty("user.dir") + File.separator + "schema.sql"),
                    StandardCharsets.US_ASCII
            );

            for (String sql : content.split(";"))
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(sql);
                } catch (SQLException e) {
                    LOGGER.warning("Warning executing schema.sql script: " + e.getMessage());
                }

        } catch (IOException e) {
            LOGGER.error("Error reading schema.sql: " + e.getMessage());
        }
    }

    public synchronized void close() {
        try {
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Users

    public synchronized int addUser(String username, String password) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO users (username, password, createdAt) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setInt(3, Utils.getTimeStamp());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next())
                    return rs.getInt(1);
                throw new SQLException("msgId not found");
            }
        }
    }

    public synchronized void updateUserToken(int userId, String authToken) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE users SET authToken = ?, expiry = ? WHERE id = ?")) {
            stmt.setString(1, authToken);
            stmt.setInt(2, Utils.getTimeStamp() + Token.EXPIRY);
            stmt.setInt(3, userId);
            stmt.executeUpdate();
        }
    }

    public synchronized void removeUserToken(int userId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE users SET authToken = ?, expiry = ? WHERE id = ?")) {
            stmt.setNull(1, Types.NULL);
            stmt.setInt(2, 0);
            stmt.setInt(3, userId);
            stmt.executeUpdate();
        }
    }

    public synchronized int getUserIdByToken(String authToken) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE authToken LIKE ? AND expiry > ?")) {
            stmt.setString(1, authToken);
            stmt.setInt(2, Utils.getTimeStamp());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                        return rs.getInt("id");
                throw new SQLException("authToken not found");
            }
        }
    }

    public synchronized boolean isTokenExpired(String authToken) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT expiry FROM users WHERE authToken LIKE ?")) {
            stmt.setString(1, authToken);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt("expiry") < Utils.getTimeStamp();
                return true;
            }
        }
    }

    public synchronized int getUserId(String username) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE username LIKE ?")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt("id");
                throw new SQLException("Username not found");
            }
        }
    }

    public synchronized String getUserPassword(int userId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT password FROM users WHERE id = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getString("password");
                throw new SQLException("UserId not found");
            }
        }
    }

    public synchronized String getUsernameByUserId(int userId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT username FROM users WHERE id = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getString("username");
                throw new SQLException("Username not found");
            }
        }
    }


    // Messages

    public synchronized int addMessage(int userId, String text) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO messages (userId, text, createdAt) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setString(2, text);
            stmt.setInt(3, Utils.getTimeStamp());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next())
                    return rs.getInt(1);
                throw new SQLException("msgId not found");
            }
        }
    }

    // TODO: Ending this little piece of sh*t
    public synchronized int getMessages(int limit, int offset) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT userId, text FROM messages ORDER BY msdId DESC LIMIT ? OFFSET ?")) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
                throw new SQLException("msgId not found");
            }
        }
    }
}
