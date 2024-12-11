package com.chat.servidor.dao;

import com.chat.shared.Conversation;
import com.chat.servidor.util.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConversationDAO {


	public Conversation createConversation(String name, int statusId, int creatorId, List<Integer> userIds) throws SQLException {
	    String createConversationSql = "INSERT INTO conversations (name, status_id, creator_id) VALUES (?, ?, ?)";
	    String addUserSql = "INSERT INTO conversation_users (conversation_id, user_id) VALUES (?, ?)";

	    try (Connection conn = DatabaseConfig.getConnection();
	         PreparedStatement createStmt = conn.prepareStatement(createConversationSql, Statement.RETURN_GENERATED_KEYS);
	         PreparedStatement addUserStmt = conn.prepareStatement(addUserSql)) {

	        createStmt.setString(1, name);
	        createStmt.setInt(2, statusId);
	        createStmt.setInt(3, creatorId);
	        createStmt.executeUpdate();


	        try (ResultSet generatedKeys = createStmt.getGeneratedKeys()) {
	            if (generatedKeys.next()) {
	                int conversationId = generatedKeys.getInt(1);
	                LocalDateTime createdAt = LocalDateTime.now();

	                addUserStmt.setInt(1, conversationId);
	                addUserStmt.setInt(2, creatorId);
	                addUserStmt.executeUpdate();

	                userIds.removeIf(id -> id == creatorId);

	                // Añadir otros usuarios
	                for (int userId : userIds) {
	                    addUserStmt.setInt(1, conversationId);
	                    addUserStmt.setInt(2, userId);
	                    addUserStmt.addBatch();
	                }
	                addUserStmt.executeBatch();

	                return new Conversation(conversationId, name, statusId, createdAt);
	            } else {
	                throw new SQLException("No se pudo obtener el ID de la conversación creada.");
	            }
	        }
	    }
	}
    /**
     * Agregar usuarios a una conversación.
     */
    public boolean addUsersToConversation(int conversationId, List<Integer> userIds) throws SQLException {
        String sql = "INSERT INTO conversation_users (conversation_id, user_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int userId : userIds) {
                stmt.setInt(1, conversationId);
                stmt.setInt(2, userId);
                stmt.addBatch();
            }
            int[] results = stmt.executeBatch();
            return results.length == userIds.size();
        }
    }


    public List<Conversation> getConversationsByUserId(int userId) throws SQLException {
        String sql = "SELECT c.id, c.name, c.status_id, c.created_at " +
                     "FROM conversations c " +
                     "JOIN conversation_users cu ON c.id = cu.conversation_id " +
                     "WHERE cu.user_id = ?";

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


    public boolean canSendMessage(int userId, int conversationId) throws SQLException {
        String sql = "SELECT 1 FROM conversation_users WHERE user_id = ? AND conversation_id = ? LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, conversationId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }


    public List<Integer> getUserIdsInConversation(int conversationId) throws SQLException {
        String sql = "SELECT user_id FROM conversation_users WHERE conversation_id = ?";
        List<Integer> userIds = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, conversationId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getInt("user_id"));
                }
            }
        }

        return userIds;
    }
}
