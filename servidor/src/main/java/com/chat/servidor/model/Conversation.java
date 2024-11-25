package com.chat.servidor.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Conversation implements Serializable { // Implementa Serializable
    private static final long serialVersionUID = 1L; // Agrega un serialVersionUID recomendado
    private int id;
    private String name;
    private int statusId;
    private LocalDateTime createdAt;

    public Conversation(int id, String name, int statusId, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.statusId = statusId;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getStatusId() {
        return statusId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    @Override
    public String toString() {
        return "Conversation{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", statusId=" + statusId +
               ", createdAt=" + createdAt +
               '}';
    }

}
