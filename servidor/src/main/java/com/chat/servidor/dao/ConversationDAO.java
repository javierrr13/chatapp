package com.chat.servidor.dao;

import com.chat.servidor.model.Conversation;
import com.chat.servidor.util.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConversationDAO {
    public boolean createConversation(String name, int statusId) throws SQLException {
        String sql = "INSERT INTO conversations (name, status_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, statusId);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Conversation> getAllConversations() throws SQLException {
        String sql = "SELECT * FROM conversations";
        List<Conversation> conversations = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Conversation conversation = new Conversation(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("status_id"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                conversations.add(conversation);
            }
        }
        return conversations;
    }

    public boolean updateConversationStatus(int conversationId, int statusId) throws SQLException {
        String sql = "UPDATE conversations SET status_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, statusId);
            stmt.setInt(2, conversationId);
            return stmt.executeUpdate() > 0;
        }
    }public List<Conversation> getConversationsByUserId(int userId) throws SQLException {
        String sql = " SELECT c.id, c.name, c.status_id, c.created_at\r\n"
        		+ "                FROM conversations c\r\n"
        		+ "                JOIN user_conversations uc ON c.id = uc.conversation_id\r\n"
        		+ "                WHERE uc.user_id = ?";

            List<Conversation> conversations = new ArrayList<>();
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, userId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Conversation conversation = new Conversation(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getInt("status_id"),
                                rs.getTimestamp("created_at").toLocalDateTime()
                        );
                        conversations.add(conversation);
                    }
                }
            }
            return conversations;
        }
    
}
