package com.messaging.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User model representing connected users
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Status {
        ONLINE,
        AWAY,
        BUSY,
        OFFLINE
    }
    
    private String username;
    private String displayName;
    private Status status;
    private LocalDateTime lastSeen;
    private LocalDateTime connectedAt;
    
    public User() {
        this.status = Status.OFFLINE;
        this.lastSeen = LocalDateTime.now();
    }
    
    public User(String username) {
        this();
        this.username = username;
        this.displayName = username;
    }
    
    public User(String username, String displayName) {
        this(username);
        this.displayName = displayName;
    }
    
    // Getters and setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
        this.lastSeen = LocalDateTime.now();
    }
    
    public LocalDateTime getLastSeen() {
        return lastSeen;
    }
    
    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }
    
    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }
    
    public void setConnectedAt(LocalDateTime connectedAt) {
        this.connectedAt = connectedAt;
    }
    
    public boolean isOnline() {
        return status == Status.ONLINE;
    }
    
    public String getStatusEmoji() {
        return switch (status) {
            case ONLINE -> "🟢";
            case AWAY -> "🟡";
            case BUSY -> "🔴";
            case OFFLINE -> "⚫";
        };
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return username != null && username.equals(user.username);
    }
    
    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return String.format("%s %s (%s)", getStatusEmoji(), displayName, status);
    }
}
