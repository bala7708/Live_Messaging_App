# BUILD AND RUN - Live Messaging App

## Quick Build & Run Instructions

### Step 1: Build the Project

```bash
mvn clean package
```

This will create TWO executable JARs in the `target/` directory:
- `messaging-server.jar` - The server
- `messaging-client.jar` - The client

### Step 2: Run the Server

Open a terminal and run:

```bash
java -jar target/messaging-server.jar
```

Or use the script:
```bash
./run-server.sh         # Linux/Mac
run-server.bat          # Windows
```

You should see:
```
╔═══════════════════════════════════════╗
║    LIVE MESSAGING SERVER STARTED     ║
║    Port: 5000                        ║
╚═══════════════════════════════════════╝
```

### Step 3: Run Client(s)

Open another terminal and run:

```bash
java -jar target/messaging-client.jar
```

Or use the script:
```bash
./run-client.sh         # Linux/Mac
run-client.bat          # Windows
```

A login window will appear. Enter:
- **Username**: Alice
- **Server**: localhost
- **Port**: 5000
- Click **Connect**

### Step 4: Run More Clients

Repeat Step 3 in new terminals with different usernames:
- Bob
- Charlie
- etc.

Now everyone can chat!

## Alternative: Run Without Building

If you want to run directly without building JARs:

### Run Server:
```bash
mvn exec:java -Dexec.mainClass="com.messaging.server.MessagingServer"
```

### Run Client:
```bash
mvn exec:java -Dexec.mainClass="com.messaging.client.MessagingClient"
```

## Troubleshooting

### "No main manifest attribute"

This means the JAR doesn't have the main class defined. Solution:
```bash
# Rebuild with clean
mvn clean package

# Then run with the full JAR names:
java -jar target/messaging-server.jar
java -jar target/messaging-client.jar
```

### "Class not found"

Make sure you're using Java 17 or higher:
```bash
java -version
```

If not, update Java and try again.

### Port Already in Use

If port 5000 is busy, run server on different port:
```bash
java -jar target/messaging-server.jar 8080
```

Then connect clients to port 8080.

## Quick Test

1. Terminal 1: `java -jar target/messaging-server.jar`
2. Terminal 2: `java -jar target/messaging-client.jar` (login as Alice)
3. Terminal 3: `java -jar target/messaging-client.jar` (login as Bob)
4. Alice and Bob can now chat!

## File Locations After Build

```
target/
├── messaging-server.jar          ← Run this for server
├── messaging-client.jar          ← Run this for client
├── live-messaging-app-1.0.0.jar  ← Original (don't use)
└── ... (other files)
```

Always use `messaging-server.jar` and `messaging-client.jar` - they have all dependencies included!

---

**Need Help?** Check README.md for full documentation.
