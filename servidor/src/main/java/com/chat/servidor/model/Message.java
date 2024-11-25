package com.chat.servidor.model;

import java.time.LocalDateTime;

public class Message {
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
}
