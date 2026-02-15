# Architecture Documentation 🏗️

## System Architecture

### High-Level Overview

```
┌──────────────────────────────────────────────────────────────┐
│                     CLIENT APPLICATIONS                      │
│                                                              │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐             │
│  │ Client 1 │    │ Client 2 │    │ Client N │             │
│  │  (Alice) │    │   (Bob)  │    │ (Charlie)│             │
│  └────┬─────┘    └────┬─────┘    └────┬─────┘             │
│       │               │               │                     │
│       │   TCP/IP      │   TCP/IP      │   TCP/IP          │
│       │   Sockets     │   Sockets     │   Sockets         │
└───────┼───────────────┼───────────────┼─────────────────────┘
        │               │               │
        │               │               │
        └───────────────┼───────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────────────────────┐
│                    MESSAGING SERVER                          │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │             ServerSocket (Port 5000)                │    │
│  └───────────────────┬────────────────────────────────┘    │
│                      │                                       │
│                      ▼                                       │
│  ┌────────────────────────────────────────────────────┐    │
│  │        Thread Pool (100 threads)                   │    │
│  ├────────────────────────────────────────────────────┤    │
│  │  ClientHandler  ClientHandler  ClientHandler ...   │    │
│  │      Thread         Thread        Thread           │    │
│  └────────────────────────────────────────────────────┘    │
│                      │                                       │
│                      ▼                                       │
│  ┌────────────────────────────────────────────────────┐    │
│  │   ConcurrentHashMap<Username, ClientHandler>      │    │
│  │   (Thread-safe user management)                    │    │
│  └────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────┘
```

## Component Architecture

### Server Components

```
MessagingServer
├── ServerSocket (Listener)
├── ExecutorService (Thread Pool)
├── Map<String, ClientHandler> (Active Clients)
├── Map<String, User> (User Data)
└── ClientHandler (Per-client thread)
    ├── Socket
    ├── BufferedReader (Input)
    ├── PrintWriter (Output)
    └── Message Handler
```

### Client Components

```
MessagingClient
├── LoginDialog (UI)
├── Socket (Server Connection)
├── BufferedReader (Input)
├── PrintWriter (Output)
├── Message Listener Thread
└── ChatWindow (UI)
    ├── JTextPane (Chat Display)
    ├── JList<String> (User List)
    ├── JTextField (Message Input)
    └── JComboBox (Recipient Selector)
```

## Message Flow

### Public Message

```
Client A                Server                Client B
   │                      │                      │
   │─── TEXT Message ────>│                      │
   │    (to: Everyone)    │                      │
   │                      │                      │
   │                      │─── Broadcast ───────>│
   │<──── Confirm ────────│                      │
   │                      │                      │
```

### Private Message

```
Client A                Server                Client B
   │                      │                      │
   │─── PRIVATE Msg ─────>│                      │
   │    (to: Bob)         │                      │
   │                      │                      │
   │                      │─── Forward ─────────>│
   │<──── Confirm ────────│                      │
   │                      │                      │
```

### User Join

```
Client                  Server              Other Clients
   │                      │                      │
   │─── LOGIN ───────────>│                      │
   │                      │                      │
   │<─── Welcome ─────────│                      │
   │                      │                      │
   │                      │─── "Alice joined" ──>│
   │                      │─── USER_LIST ───────>│
   │<─── USER_LIST ───────│                      │
```

## Data Models

### Message Model

```
Message {
    MessageType type     // TEXT, LOGIN, LOGOUT, etc.
    String sender        // Username
    String receiver      // For private messages (null = broadcast)
    String content       // Message text
    LocalDateTime timestamp
    String messageId     // Unique ID
}
```

### User Model

```
User {
    String username
    String displayName
    Status status           // ONLINE, AWAY, BUSY, OFFLINE
    LocalDateTime lastSeen
    LocalDateTime connectedAt
}
```

## Threading Model

### Server Threading

```
Main Thread
    │
    └─> Accept Loop (Blocking)
            │
            ├─> Thread Pool
            │   ├─> ClientHandler Thread 1
            │   ├─> ClientHandler Thread 2
            │   ├─> ClientHandler Thread 3
            │   └─> ...
            │
            └─> Each ClientHandler:
                ├─> Read Loop (Blocking)
                ├─> Message Processing
                └─> Write to Socket
```

### Client Threading

```
Main Thread (UI - Event Dispatch Thread)
    │
    └─> Connection Thread
            │
            └─> Message Listener Thread
                ├─> Read Loop (Blocking)
                └─> Update UI (via SwingUtilities.invokeLater)
```

## Communication Protocol

### JSON Message Format

```json
{
  "type": "TEXT",
  "sender": "Alice",
  "receiver": null,
  "content": "Hello everyone!",
  "timestamp": "2024-01-15T12:30:45",
  "messageId": "1705326645000-1234"
}
```

### Message Types

| Type | Direction | Purpose |
|------|-----------|---------|
| LOGIN | Client → Server | Authenticate user |
| LOGOUT | Client → Server | Disconnect user |
| TEXT | Bidirectional | Chat message |
| PRIVATE | Bidirectional | Direct message |
| SYSTEM | Server → Client | Notification |
| USER_LIST | Server → Client | Online users |
| TYPING | Client → Server | Typing indicator |

## Concurrency & Thread Safety

### Thread-Safe Components

1. **ConcurrentHashMap**: User management
   ```java
   private final Map<String, ClientHandler> connectedClients;
   private final Map<String, User> users;
   ```

2. **Synchronized Socket I/O**: Each ClientHandler has its own streams
   ```java
   private BufferedReader reader;  // Per-client
   private PrintWriter writer;      // Per-client
   ```

3. **Thread Pool**: Managed by ExecutorService
   ```java
   private final ExecutorService threadPool = 
       Executors.newFixedThreadPool(THREAD_POOL_SIZE);
   ```

### Race Condition Prevention

- User list updates are atomic
- Message broadcasts use iterator over concurrent map
- No shared mutable state between ClientHandlers

## Error Handling

### Client Disconnection

```
ClientHandler.run()
    │
    ├─> try {
    │       // Read messages
    │   }
    ├─> catch (IOException) {
    │       // Handle error
    │   }
    └─> finally {
            disconnect();  // Cleanup
            remove from maps
            notify other users
        }
```

### Network Failures

- **Automatic cleanup** when socket closes
- **Graceful degradation** with try-catch blocks
- **Resource cleanup** in finally blocks
- **User notification** via system messages

## Performance Optimization

### Server Optimizations

1. **Thread Pooling**: Reuse threads instead of creating new ones
2. **Concurrent Collections**: Lock-free operations
3. **Non-blocking Broadcasts**: Iterate and send without global locks
4. **Efficient JSON**: Gson for fast serialization

### Client Optimizations

1. **Separate Threads**: UI doesn't block on I/O
2. **SwingUtilities.invokeLater**: Thread-safe UI updates
3. **Styled Documents**: Efficient text rendering
4. **Lazy Updates**: Only update when needed

## Scalability

### Current Limits

- **Max Clients**: 100 (thread pool size)
- **Messages/sec**: ~1000 per client
- **Network**: Limited by bandwidth
- **Memory**: ~50MB per 100 clients

### Scaling Strategies

1. **Horizontal**: Multiple server instances with load balancer
2. **Vertical**: Increase thread pool size
3. **Async I/O**: Use NIO for better scalability
4. **Database**: Add persistence layer
5. **Message Queue**: Decouple sending from processing

## Security Considerations

### Current Implementation

- ⚠️ **No encryption** (plaintext communication)
- ⚠️ **No authentication** (username only)
- ⚠️ **No authorization** (all users equal)
- ⚠️ **No input sanitization**

### Production Requirements

1. **SSL/TLS**: Encrypt all communication
2. **Authentication**: Password/token-based
3. **Authorization**: Role-based access control
4. **Input Validation**: Prevent injection attacks
5. **Rate Limiting**: Prevent flooding
6. **Audit Logging**: Track all actions

## Deployment Architecture

### Development

```
Localhost
├── Server (Port 5000)
└── Clients (multiple instances)
```

### Production

```
Internet
    │
    ▼
Load Balancer (HTTPS)
    │
    ├── Server 1 (Port 5000)
    ├── Server 2 (Port 5001)
    └── Server N (Port 500N)
    │
    └── Database (Shared State)
```

## Technology Stack

| Layer | Technology |
|-------|------------|
| Language | Java 17 |
| Build Tool | Maven 3.6+ |
| UI Framework | Swing + FlatLaf |
| Networking | Java Sockets (TCP) |
| Serialization | Gson (JSON) |
| Concurrency | ExecutorService |
| Logging | SLF4J |
| Testing | JUnit 5 |

## Design Patterns Used

1. **Client-Server**: Network architecture
2. **Observer**: Message notification
3. **Thread Pool**: Resource management
4. **Producer-Consumer**: Message queue
5. **Singleton**: Server instance
6. **MVC**: Separation of concerns
7. **Factory**: Message creation

---

**Version**: 1.0.0  
**Last Updated**: 2025
