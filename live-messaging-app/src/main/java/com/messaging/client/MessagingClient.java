package com.messaging.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.messaging.model.Message;
import com.messaging.ui.ChatWindow;
import com.messaging.util.LocalDateTimeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Messaging client handling server communication
 */
public class MessagingClient {
    private static final Logger logger = LoggerFactory.getLogger(MessagingClient.class);
    
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;
    private ChatWindow chatWindow;
    private final Gson gson;
    private boolean connected;
    
    public MessagingClient() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        this.connected = false;
    }
    
    public boolean connect(String host, int port, String username) {
        try {
            this.username = username;
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
            
            // Send login message
            Message loginMessage = new Message(Message.MessageType.LOGIN, username, "");
            sendMessage(loginMessage);
            
            // Start message listener thread
            new Thread(this::listenForMessages).start();
            
            logger.info("Connected to server at {}:{}", host, port);
            return true;
            
        } catch (IOException e) {
            logger.error("Failed to connect to server", e);
            JOptionPane.showMessageDialog(
                null,
                "Could not connect to server at " + host + ":" + port,
                "Connection Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }
    
    private void listenForMessages() {
        try {
            String line;
            while (connected && (line = reader.readLine()) != null) {
                handleIncomingMessage(line);
            }
        } catch (IOException e) {
            if (connected) {
                logger.error("Error reading from server", e);
                disconnect();
            }
        }
    }
    
    private void handleIncomingMessage(String json) {
        try {
            Message message = gson.fromJson(json, Message.class);
            
            switch (message.getType()) {
                case TEXT, PRIVATE:
                    if (chatWindow != null) {
                        chatWindow.displayMessage(message);
                    }
                    break;
                
                case SYSTEM:
                    if (chatWindow != null) {
                        chatWindow.displaySystemMessage(message.getContent());
                    }
                    break;
                
                case USER_LIST:
                    List<String> users = gson.fromJson(
                        message.getContent(),
                        new TypeToken<List<String>>(){}.getType()
                    );
                    if (chatWindow != null) {
                        chatWindow.updateUserList(users);
                    }
                    break;
                
                case TYPING:
                    if (chatWindow != null) {
                        chatWindow.showTypingIndicator(message.getSender());
                    }
                    break;
                
                default:
                    logger.debug("Received message: {}", message);
            }
            
        } catch (Exception e) {
            logger.error("Error handling incoming message", e);
        }
    }
    
    public void sendMessage(Message message) {
        if (connected && writer != null) {
            String json = gson.toJson(message);
            writer.println(json);
        }
    }
    
    public void sendTextMessage(String content) {
        Message message = new Message(Message.MessageType.TEXT, username, content);
        sendMessage(message);
    }
    
    public void sendPrivateMessage(String receiver, String content) {
        Message message = new Message(Message.MessageType.PRIVATE, username, receiver, content);
        sendMessage(message);
    }
    
    public void sendTypingIndicator() {
        Message message = new Message(Message.MessageType.TYPING, username, "typing...");
        sendMessage(message);
    }
    
    public void disconnect() {
        connected = false;
        
        try {
            if (writer != null) {
                Message logoutMessage = new Message(Message.MessageType.LOGOUT, username, "");
                sendMessage(logoutMessage);
            }
            
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            
            logger.info("Disconnected from server");
            
        } catch (IOException e) {
            logger.error("Error disconnecting", e);
        }
    }
    
    public void setChatWindow(ChatWindow chatWindow) {
        this.chatWindow = chatWindow;
    }
    
    public String getUsername() {
        return username;
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public static void main(String[] args) {
        // Set FlatLaf look and feel
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
        } catch (Exception e) {
            logger.warn("Could not set FlatLaf look and feel", e);
        }
        
        SwingUtilities.invokeLater(() -> {
            // Show login dialog
            LoginDialog loginDialog = new LoginDialog();
            loginDialog.setVisible(true);
        });
    }
}
