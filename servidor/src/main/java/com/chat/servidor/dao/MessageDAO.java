package com.chat.servidor.dao;

import com.chat.servidor.model.Message;
import com.chat.servidor.util.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {
    public boolean addMessage(int conversationId, int userId, String content) throws SQLException {
        String sql = "INSERT INTO messages (conversation_id, user_id, content) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, conversationId);
            stmt.setInt(2, userId);
            stmt.setString(3, content);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Message> getMessagesByConversation(int conversationId) throws SQLException {
        String sql = "SELECT * FROM messages WHERE conversation_id = ? ORDER BY sent_at ASC";
        List<Message> messages = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, conversationId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Message message = new Message(
                        rs.getInt("id"),
                        rs.getInt("conversation_id"),
                        rs.getInt("user_id"),
                        rs.getString("content"),
                        rs.getTimestamp("sent_at").toLocalDateTime()
                );
                messages.add(message);
            }
        }
        return messages;
    }
    public List<Message> getMessagesByConversationId(int conversationId) throws SQLException {
        String sql = "SELECT * FROM messages WHERE conversation_id = ? ORDER BY sent_at ASC";
        List<Message> messages = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, conversationId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Message message = new Message(
                            rs.getInt("id"),
                            rs.getInt("conversation_id"),
                            rs.getInt("user_id"),
                            rs.getString("content"),
                            rs.getTimestamp("sent_at").toLocalDateTime()
                    );
                    messages.add(message);
                }
            }
        }
        return messages;
    }

}
