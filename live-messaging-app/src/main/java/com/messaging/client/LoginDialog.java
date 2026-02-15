package com.messaging.client;

import com.messaging.ui.ChatWindow;

import javax.swing.*;
import java.awt.*;

/**
 * Login dialog for user authentication
 */
public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JTextField serverField;
    private JTextField portField;
    private JButton connectButton;
    private JButton cancelButton;
    
    public LoginDialog() {
        setTitle("Connect to Messaging Server");
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        initComponents();
        pack();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Title
        JLabel titleLabel = new JLabel("Live Messaging");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        
        // Username
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        panel.add(usernameField, gbc);
        
        // Server
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Server:"), gbc);
        
        gbc.gridx = 1;
        serverField = new JTextField("localhost");
        panel.add(serverField, gbc);
        
        // Port
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Port:"), gbc);
        
        gbc.gridx = 1;
        portField = new JTextField("5000");
        panel.add(portField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> handleConnect());
        buttonPanel.add(connectButton);
        
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> System.exit(0));
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        add(panel);
        
        // Set default button
        getRootPane().setDefaultButton(connectButton);
        
        // Focus username field
        usernameField.requestFocusInWindow();
    }
    
    private void handleConnect() {
        String username = usernameField.getText().trim();
        String server = serverField.getText().trim();
        String portText = portField.getText().trim();
        
        // Validation
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter a username",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
            usernameField.requestFocus();
            return;
        }
        
        if (server.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter server address",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
            serverField.requestFocus();
            return;
        }
        
        int port;
        try {
            port = Integer.parseInt(portText);
            if (port < 1 || port > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter a valid port number (1-65535)",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
            portField.requestFocus();
            return;
        }
        
        // Disable button during connection
        connectButton.setEnabled(false);
        connectButton.setText("Connecting...");
        
        // Connect in background thread
        new Thread(() -> {
            MessagingClient client = new MessagingClient();
            boolean connected = client.connect(server, port, username);
            
            SwingUtilities.invokeLater(() -> {
                if (connected) {
                    // Open chat window
                    ChatWindow chatWindow = new ChatWindow(client);
                    client.setChatWindow(chatWindow);
                    chatWindow.setVisible(true);
                    dispose();
                } else {
                    connectButton.setEnabled(true);
                    connectButton.setText("Connect");
                }
            });
        }).start();
    }
}
