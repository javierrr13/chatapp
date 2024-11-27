package com.chat.servidor.dao;

import com.chat.servidor.util.DatabaseConfig;
import com.chat.shared.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {
    /**
     * Agregar un mensaje a una conversación si el usuario pertenece a la misma.
     */
    public boolean addMessage(int conversationId, int userId, String content) throws SQLException {
        String checkMembershipSql = "   SELECT 1\r\n"
        		+ "            FROM conversation_users\r\n"
        		+ "            WHERE conversation_id = ? AND user_id = ?\r\n"
        		+ "            LIMIT 1;";

        String insertMessageSql = "INSERT INTO messages (conversation_id, user_id, content)\r\n"
        		+ "            VALUES (?, ?, ?);";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkMembershipSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertMessageSql)) {

            // Verificar si el usuario pertenece a la conversación
            checkStmt.setInt(1, conversationId);
            checkStmt.setInt(2, userId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                throw new SQLException("El usuario no pertenece a la conversación.");
            }

            // Insertar el mensaje
            insertStmt.setInt(1, conversationId);
            insertStmt.setInt(2, userId);
            insertStmt.setString(3, content);
            return insertStmt.executeUpdate() > 0;
        }
    }

    /**
     * Obtener mensajes de una conversación, ordenados por fecha.
     */
    public List<Message> getMessagesByConversation(int conversationId) throws SQLException {
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
