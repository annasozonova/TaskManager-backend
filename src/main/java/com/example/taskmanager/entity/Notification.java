package com.example.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity representing a notification for a user. A notification can be of different types
 * (e.g., related to a task or user) and has properties such as message, read status, and timestamp.
 */
@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String message;
    private boolean read;
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private NotificationType type;

    private Integer referenceId;

    // Constructors, Getters and Setters

    /**
     * Enum representing the possible types of notifications.
     */
    public enum NotificationType {
        @JsonProperty("TASK")
        TASK,

        @JsonProperty("USER")
        USER,

        @JsonProperty("OTHER")
        OTHER
    }

    public Notification() {}

    /**
     * Returns a string representation of the notification, including the user's username, message,
     * read status, timestamp, type, and reference ID.
     *
     * @return the string representation of the notification
     */
    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", message='" + message + '\'' +
                ", read=" + read +
                ", timestamp=" + timestamp +
                ", type=" + type +
                ", referenceId=" + referenceId +
                '}';
    }

    public Notification(User user, String message, boolean read, LocalDateTime timestamp, NotificationType type, Integer referenceId) {
        this.user = user;
        this.message = message;
        this.read = read;
        this.timestamp = timestamp;
        this.type = type;
        this.referenceId = referenceId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Integer referenceId) {
        this.referenceId = referenceId;
    }
}
