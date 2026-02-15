package com.messaging.ui;

import com.messaging.client.MessagingClient;
import com.messaging.model.Message;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Main chat window UI
 */
public class ChatWindow extends JFrame {
    private final MessagingClient client;
    
    private JTextPane chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private JLabel statusLabel;
    private JComboBox<String> recipientComboBox;
    private StyledDocument doc;
    
    private Style myMessageStyle;
    private Style otherMessageStyle;
    private Style systemMessageStyle;
    private Style timestampStyle;
    
    public ChatWindow(MessagingClient client) {
        this.client = client;
        
        setTitle("Live Messaging - " + client.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        
        initComponents();
        initStyles();  // Call after initComponents so chatArea exists
        
        // Handle window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.disconnect();
            }
        });
    }
    
    private void initStyles() {
        // Text styles for different message types
        myMessageStyle = chatArea.addStyle("myMessageStyle", null);
        StyleConstants.setForeground(myMessageStyle, new Color(100, 181, 246));
        StyleConstants.setBold(myMessageStyle, true);
        
        otherMessageStyle = chatArea.addStyle("otherMessageStyle", null);
        StyleConstants.setForeground(otherMessageStyle, new Color(255, 193, 7));
        StyleConstants.setBold(otherMessageStyle, true);
        
        systemMessageStyle = chatArea.addStyle("systemMessageStyle", null);
        StyleConstants.setForeground(systemMessageStyle, new Color(158, 158, 158));
        StyleConstants.setItalic(systemMessageStyle, true);
        StyleConstants.setAlignment(systemMessageStyle, StyleConstants.ALIGN_CENTER);
        
        timestampStyle = chatArea.addStyle("timestampStyle", null);
        StyleConstants.setForeground(timestampStyle, new Color(117, 117, 117));
        StyleConstants.setFontSize(timestampStyle, 10);
    }
    
    private void initComponents() {
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Chat area
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        doc = chatArea.getStyledDocument();
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // User list
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setBorder(BorderFactory.createTitledBorder("Online Users"));
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(180, 0));
        
        // Message input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        
        // Recipient selector
        JPanel topInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topInputPanel.add(new JLabel("To:"));
        recipientComboBox = new JComboBox<>(new String[]{"Everyone"});
        recipientComboBox.setPreferredSize(new Dimension(150, 25));
        topInputPanel.add(recipientComboBox);
        
        messageField = new JTextField();
        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
            
            @Override
            public void keyTyped(KeyEvent e) {
                // Send typing indicator
                client.sendTypingIndicator();
            }
        });
        
        sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(80, 30));
        sendButton.addActionListener(e -> sendMessage());
        
        JPanel bottomInputPanel = new JPanel(new BorderLayout(5, 5));
        bottomInputPanel.add(messageField, BorderLayout.CENTER);
        bottomInputPanel.add(sendButton, BorderLayout.EAST);
        
        inputPanel.add(topInputPanel, BorderLayout.NORTH);
        inputPanel.add(bottomInputPanel, BorderLayout.CENTER);
        
        // Status bar
        statusLabel = new JLabel("Connected as " + client.getUsername());
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Assemble UI
        mainPanel.add(chatScroll, BorderLayout.CENTER);
        mainPanel.add(userScroll, BorderLayout.EAST);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        
        // Menu bar
        createMenuBar();
        
        // Welcome message
        displaySystemMessage("Welcome to Live Messaging! You are connected as " + client.getUsername());
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem clearMenuItem = new JMenuItem("Clear Chat");
        clearMenuItem.addActionListener(e -> clearChat());
        fileMenu.add(clearMenuItem);
        
        fileMenu.addSeparator();
        
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(e -> {
            client.disconnect();
            System.exit(0);
        });
        fileMenu.add(exitMenuItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        
        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutMenuItem);
        
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void sendMessage() {
        String text = messageField.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        
        String recipient = (String) recipientComboBox.getSelectedItem();
        
        if (recipient != null && !recipient.equals("Everyone")) {
            // Private message
            client.sendPrivateMessage(recipient, text);
        } else {
            // Public message
            client.sendTextMessage(text);
        }
        
        messageField.setText("");
        messageField.requestFocus();
    }
    
    public void displayMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            try {
                boolean isMyMessage = message.getSender().equals(client.getUsername());
                boolean isPrivate = message.isPrivate();
                
                // Timestamp
                doc.insertString(doc.getLength(), "[" + message.getFormattedTimestamp() + "] ", timestampStyle);
                
                // Sender
                Style senderStyle = isMyMessage ? myMessageStyle : otherMessageStyle;
                doc.insertString(doc.getLength(), message.getSender(), senderStyle);
                
                // Private indicator
                if (isPrivate) {
                    doc.insertString(doc.getLength(), " (private to " + message.getReceiver() + ")", timestampStyle);
                }
                
                // Message content
                doc.insertString(doc.getLength(), ": " + message.getContent() + "\n", null);
                
                // Auto-scroll to bottom
                chatArea.setCaretPosition(doc.getLength());
                
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
    
    public void displaySystemMessage(String text) {
        SwingUtilities.invokeLater(() -> {
            try {
                doc.insertString(doc.getLength(), "*** " + text + " ***\n", systemMessageStyle);
                chatArea.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
    
    public void updateUserList(List<String> users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            recipientComboBox.removeAllItems();
            recipientComboBox.addItem("Everyone");
            
            for (String user : users) {
                if (!user.equals(client.getUsername())) {
                    userListModel.addElement("🟢 " + user);
                    recipientComboBox.addItem(user);
                }
            }
            
            statusLabel.setText("Connected as " + client.getUsername() + " | " + users.size() + " users online");
        });
    }
    
    public void showTypingIndicator(String username) {
        // Could implement a typing indicator UI here
        // For now, just log it
        if (!username.equals(client.getUsername())) {
            System.out.println(username + " is typing...");
        }
    }
    
    private void clearChat() {
        try {
            doc.remove(0, doc.getLength());
            displaySystemMessage("Chat cleared");
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    private void showAboutDialog() {
        String message = "Live Messaging Application\n\n" +
                        "Version: 1.0.0\n" +
                        "A real-time chat application built with Java\n\n" +
                        "Features:\n" +
                        "• Real-time messaging\n" +
                        "• Private messaging\n" +
                        "• User presence\n" +
                        "• Modern UI";
        
        JOptionPane.showMessageDialog(
            this,
            message,
            "About",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
