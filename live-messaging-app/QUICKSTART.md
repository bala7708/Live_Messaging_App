# Quick Start Guide 🚀

Get your live messaging app running in 5 minutes!

## Step 1: Prerequisites

Ensure you have:
- **Java 17+** installed
- **Maven 3.6+** installed

Check versions:
```bash
java -version
mvn -version
```

## Step 2: Build the Application

```bash
cd live-messaging-app
mvn clean package
```

This creates two JAR files in `target/`:
- `live-messaging-app-1.0.0-server.jar` (Server)
- `messaging-client.jar` (Client)

## Step 3: Start the Server

Open a terminal:

```bash
java -jar target/messaging-server.jar
```

You should see:
```
╔═══════════════════════════════════════╗
║    LIVE MESSAGING SERVER STARTED     ║
║    Port: 5000                        ║
╚═══════════════════════════════════════╝
```

✅ Server is running!

## Step 4: Connect First Client

Open another terminal:

```bash
java -jar target/messaging-client.jar
```

A login dialog appears:
1. **Username**: Enter "Alice"
2. **Server**: localhost
3. **Port**: 5000
4. Click **Connect**

✅ Alice is connected!

## Step 5: Connect Second Client

Open a third terminal:

```bash
java -jar target/messaging-client.jar
```

Login as:
1. **Username**: "Bob"
2. **Server**: localhost
3. **Port**: 5000
4. Click **Connect**

✅ Bob is connected!

## Step 6: Start Chatting!

In Alice's window:
```
To: Everyone
Message: Hello Bob!
[Send]
```

In Bob's window, you'll see:
```
[12:30:45] Alice: Hello Bob!
```

Bob can reply:
```
To: Everyone
Message: Hi Alice!
[Send]
```

## 🎯 Quick Test Scenarios

### Test 1: Public Chat
1. Have Alice send: "Hello everyone!"
2. Have Bob send: "Hey there!"
3. Both should see both messages

### Test 2: Private Message
1. In Alice's window, select "Bob" from dropdown
2. Send: "This is private"
3. Only Bob sees this message (marked as private)

### Test 3: Multiple Users
```bash
# Terminal 4
java -jar target/messaging-client.jar
# Login as: Charlie
```

Now all three can chat!

## 📱 What You Can Do

### Send Public Messages
- Select "Everyone" in dropdown
- Type message and hit Enter
- All connected users see it

### Send Private Messages
- Select a username from dropdown
- Type message and hit Enter
- Only that user sees it

### See Who's Online
- Check the "Online Users" list on the right
- Green dots (🟢) show online users
- Updates in real-time

## 🔧 Common Commands

### Start Server on Different Port
```bash
java -jar target/messaging-server.jar 8080
```

Then connect clients to port 8080

### Run Multiple Clients
Just run the command multiple times with different usernames!

### Stop Server
Press `Ctrl+C` in server terminal

## 🐛 Quick Fixes

**"Port already in use"**
```bash
# Use different port
java -jar server.jar 8080
```

**"Connection refused"**
- Make sure server is running first
- Check you're using correct port

**Can't connect**
- Verify server is running
- Check username is unique
- Try 127.0.0.1 instead of localhost

## 🎨 UI Overview

```
┌────────────────────────────────────────────┐
│ File  Help                                 │
├────────────────────────┬───────────────────┤
│                        │ Online Users      │
│  Chat Messages         │                   │
│  Appear Here           │ 🟢 Alice          │
│                        │ 🟢 Bob            │
│                        │                   │
├────────────────────────┴───────────────────┤
│ To: [Everyone ▼]                           │
│ Message: [____________] [Send]             │
├────────────────────────────────────────────┤
│ Status: Connected as Alice | 2 users online│
└────────────────────────────────────────────┘
```

## 📝 Example Chat Session

```
*** Welcome to Live Messaging! You are connected as Alice ***
*** Bob joined the chat ***
[12:30:00] Alice: Hey everyone!
[12:30:05] Bob: Hello!
[12:30:10] Alice (private to Bob): How are you?
[12:30:15] Bob (private to Alice): I'm good!
*** Charlie joined the chat ***
[12:30:20] Charlie: Hi all!
```

## 🌟 Pro Tips

1. **Multiple Windows**: Open several clients to test fully
2. **Different Machines**: Works across network (use server's IP)
3. **Private Messages**: Great for 1-on-1 conversations
4. **Clear Chat**: File → Clear Chat to reset window
5. **Timestamps**: Every message shows time sent

## 🎯 Next Steps

- Try connecting from another computer on your network
- Experiment with private vs. public messages
- Test with 5+ users simultaneously
- Check out the full README for advanced features

## 📚 More Information

- **Full README**: [README.md](README.md)
- **Server Logs**: Check server terminal for activity
- **Client Logs**: Debug info in client terminal

---

**That's it! You're now chatting live! 💬**

Questions? Check the troubleshooting section in the main README.
