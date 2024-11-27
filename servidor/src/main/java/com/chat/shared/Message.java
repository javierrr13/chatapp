package com.chat.shared;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L; // Versión de serialización

    private int id;
    private int conversationId;
    private int userId;
    private String content;
    private LocalDateTime sentAt;

    public Message(int id, int conversationId, int userId, String content, LocalDateTime sentAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.userId = userId;
        this.content = content;
        this.sentAt = sentAt;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getConversationId() {
        return conversationId;
    }

    public int getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    // Método toString para depuración
    @Override
    public String toString() {
        return "Message{" +
               "id=" + id +
               ", conversationId=" + conversationId +
               ", userId=" + userId +
               ", content='" + content + '\'' +
               ", sentAt=" + sentAt +
               '}';
    }
}
