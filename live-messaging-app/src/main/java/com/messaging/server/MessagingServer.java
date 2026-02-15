package com.messaging.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.messaging.model.Message;
import com.messaging.model.User;
import com.messaging.util.LocalDateTimeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Multi-threaded messaging server handling multiple client connections
 */
public class MessagingServer {
    private static final Logger logger = LoggerFactory.getLogger(MessagingServer.class);
    private static final int DEFAULT_PORT = 5000;
    private static final int THREAD_POOL_SIZE = 100;
    
    private final int port;
    private ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private final Map<String, ClientHandler> connectedClients;
    private final Map<String, User> users;
    private final Gson gson;
    private boolean running;
    
    public MessagingServer(int port) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.connectedClients = new ConcurrentHashMap<>();
        this.users = new ConcurrentHashMap<>();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        this.running = false;
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            logger.info("🚀 Messaging Server started on port {}", port);
            System.out.println("╔═══════════════════════════════════════╗");
            System.out.println("║    LIVE MESSAGING SERVER STARTED     ║");
            System.out.println("║    Port: " + port + "                          ║");
            System.out.println("╚═══════════════════════════════════════╝");
            
            acceptConnections();
            
        } catch (IOException e) {
            logger.error("Failed to start server", e);
            System.err.println("Error: Could not start server on port " + port);
        }
    }
    
    private void acceptConnections() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                logger.info("New connection from {}", clientSocket.getInetAddress());
                
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                threadPool.execute(clientHandler);
                
            } catch (IOException e) {
                if (running) {
                    logger.error("Error accepting connection", e);
                }
            }
        }
    }
    
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            threadPool.shutdown();
            logger.info("Server stopped");
        } catch (IOException e) {
            logger.error("Error stopping server", e);
        }
    }
    
    private void broadcastMessage(Message message) {
        String json = gson.toJson(message);
        for (ClientHandler client : connectedClients.values()) {
            client.sendMessage(json);
        }
        logger.debug("Broadcasted message from {}", message.getSender());
    }
    
    private void sendPrivateMessage(Message message) {
        String json = gson.toJson(message);
        
        // Send to receiver
        ClientHandler receiver = connectedClients.get(message.getReceiver());
        if (receiver != null) {
            receiver.sendMessage(json);
        }
        
        // Also send back to sender for confirmation
        ClientHandler sender = connectedClients.get(message.getSender());
        if (sender != null) {
            sender.sendMessage(json);
        }
        
        logger.debug("Private message: {} -> {}", message.getSender(), message.getReceiver());
    }
    
    private void broadcastUserList() {
        List<String> usernames = new ArrayList<>(connectedClients.keySet());
        Message userListMessage = new Message(
            Message.MessageType.USER_LIST,
            "SERVER",
            gson.toJson(usernames)
        );
        broadcastMessage(userListMessage);
    }
    
    private void notifyUserJoined(String username) {
        Message notification = new Message(
            Message.MessageType.SYSTEM,
            "SERVER",
            username + " joined the chat"
        );
        broadcastMessage(notification);
        broadcastUserList();
    }
    
    private void notifyUserLeft(String username) {
        Message notification = new Message(
            Message.MessageType.SYSTEM,
            "SERVER",
            username + " left the chat"
        );
        broadcastMessage(notification);
        broadcastUserList();
    }
    
    /**
     * Client handler thread - handles individual client connections
     */
    private class ClientHandler implements Runnable {
        private final Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private String username;
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);
                
                // Handle client messages
                String line;
                while ((line = reader.readLine()) != null) {
                    handleMessage(line);
                }
                
            } catch (IOException e) {
                logger.error("Error handling client", e);
            } finally {
                disconnect();
            }
        }
        
        private void handleMessage(String json) {
            try {
                Message message = gson.fromJson(json, Message.class);
                
                switch (message.getType()) {
                    case LOGIN:
                        handleLogin(message);
                        break;
                    
                    case LOGOUT:
                        disconnect();
                        break;
                    
                    case TEXT:
                        if (message.isPrivate()) {
                            sendPrivateMessage(message);
                        } else {
                            broadcastMessage(message);
                        }
                        break;
                    
                    case PRIVATE:
                        sendPrivateMessage(message);
                        break;
                    
                    case TYPING:
                        broadcastMessage(message);
                        break;
                    
                    case USER_LIST:
                        broadcastUserList();
                        break;
                    
                    default:
                        logger.warn("Unknown message type: {}", message.getType());
                }
                
            } catch (Exception e) {
                logger.error("Error processing message", e);
            }
        }
        
        private void handleLogin(Message message) {
            this.username = message.getSender();
            connectedClients.put(username, this);
            
            User user = new User(username);
            user.setStatus(User.Status.ONLINE);
            user.setConnectedAt(LocalDateTime.now());
            users.put(username, user);
            
            // Send success response
            Message response = new Message(
                Message.MessageType.LOGIN,
                "SERVER",
                "Welcome, " + username + "!"
            );
            sendMessage(gson.toJson(response));
            
            // Notify others
            notifyUserJoined(username);
            
            logger.info("User {} logged in", username);
            System.out.println("✓ User connected: " + username + " (Total: " + connectedClients.size() + ")");
        }
        
        private void disconnect() {
            if (username != null) {
                connectedClients.remove(username);
                users.remove(username);
                notifyUserLeft(username);
                logger.info("User {} disconnected", username);
                System.out.println("✗ User disconnected: " + username + " (Total: " + connectedClients.size() + ")");
            }
            
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                logger.error("Error closing socket", e);
            }
        }
        
        public void sendMessage(String json) {
            if (writer != null) {
                writer.println(json);
            }
        }
    }
    
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default: " + DEFAULT_PORT);
            }
        }
        
        MessagingServer server = new MessagingServer(port);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n\nShutting down server...");
            server.stop();
        }));
        
        server.start();
    }
}
