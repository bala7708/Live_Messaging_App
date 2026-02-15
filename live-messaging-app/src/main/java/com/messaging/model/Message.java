package com.messaging.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Message model representing chat messages
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum MessageType {
        TEXT,           // Regular text message
        LOGIN,          // User login request
        LOGOUT,         // User logout
        USER_LIST,      // Request/update user list
        PRIVATE,        // Private message
        GROUP,          // Group message
        SYSTEM,         // System notification
        TYPING,         // Typing indicator
        FILE,           // File transfer
        STATUS          // Status update
    }
    
    private MessageType type;
    private String sender;
    private String receiver;  // For private messages (null for broadcast)
    private String content;
    private LocalDateTime timestamp;
    private String messageId;
    
    public Message() {
        this.timestamp = LocalDateTime.now();
        this.messageId = generateMessageId();
    }
    
    public Message(MessageType type, String sender, String content) {
        this();
        this.type = type;
        this.sender = sender;
        this.content = content;
    }
    
    public Message(MessageType type, String sender, String receiver, String content) {
        this(type, sender, content);
        this.receiver = receiver;
    }
    
    private String generateMessageId() {
        return System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
    }
    
    // Getters and setters
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public String getSender() {
        return sender;
    }
    
    public void setSender(String sender) {
        this.sender = sender;
    }
    
    public String getReceiver() {
        return receiver;
    }
    
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return timestamp.format(formatter);
    }
    
    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }
    
    public boolean isPrivate() {
        return receiver != null && !receiver.isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s -> %s: %s", 
            type, 
            sender, 
            receiver != null ? receiver : "ALL", 
            content
        );
    }
}
