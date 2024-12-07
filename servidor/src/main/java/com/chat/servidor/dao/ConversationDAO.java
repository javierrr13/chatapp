package com.chat.servidor.dao;

import com.chat.shared.Conversation;
import com.chat.servidor.util.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConversationDAO {

    /**
     * Crear una conversaci�n y a�adir al creador como miembro autom�ticamente.
     */
    public Conversation createConversation(String name, int statusId, int creatorId) throws SQLException {
        String createConversationSql = "  INSERT INTO conversation_users (conversation_id, user_id)\r\n"
        		+ "            VALUES (?, ?);";

        String addUserSql ="  INSERT INTO conversation_users (conversation_id, user_id)\r\n"
        		+ "            VALUES (?, ?);";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement createStmt = conn.prepareStatement(createConversationSql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement addUserStmt = conn.prepareStatement(addUserSql)) {

            // Crear la conversaci�n
            createStmt.setString(1, name);
            createStmt.setInt(2, statusId);
            createStmt.setInt(3, creatorId);
            createStmt.executeUpdate();

            // Obtener el ID de la conversaci�n creada
            try (ResultSet generatedKeys = createStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int conversationId = generatedKeys.getInt(1);
                    LocalDateTime createdAt = LocalDateTime.now(); // Suponiendo que `created_at` tiene valor autom�tico

                    // A�adir al creador como miembro de la conversaci�n
                    addUserStmt.setInt(1, conversationId);
                    addUserStmt.setInt(2, creatorId);
                    addUserStmt.executeUpdate();

                    // Crear y devolver una instancia de Conversation
                    return new Conversation(conversationId, name, statusId, createdAt);
                } else {
                    throw new SQLException("No se pudo obtener el ID de la conversaci�n creada.");
                }
            }
        }
    }

    /**
     * Agregar usuarios a una conversaci�n.
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

    /**
     * Obtener conversaciones en las que participa un usuario.
     */
    public List<Conversation> getConversationsByUserId(int userId) throws SQLException {
        String sql = "  SELECT c.id, c.name, c.status_id, c.created_at\r\n"
        		+ "            FROM conversations c\r\n"
        		+ "            JOIN conversation_users cu ON c.id = cu.conversation_id\r\n"
        		+ "            WHERE cu.user_id = ?;";

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
        String sql ="    SELECT 1\r\n"
        		+ "            FROM conversation_users\r\n"
        		+ "            WHERE user_id = ? AND conversation_id = ?\r\n"
        		+ "            LIMIT 1;";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, conversationId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Devuelve true si el usuario pertenece a la conversaci�n
            }
        }
    }
    /**
     * Obtener los IDs de los usuarios que est�n en una conversaci�n espec�fica.
     *
     * @param conversationId El ID de la conversaci�n.
     * @return Una lista de IDs de usuarios que participan en la conversaci�n.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    public List<Integer> getUserIdsInConversation(int conversationId) throws SQLException {
        String sql = "SELECT user_id " +
                     "FROM conversation_users " +
                     "WHERE conversation_id = ?";

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
