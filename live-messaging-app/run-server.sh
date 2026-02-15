#!/bin/bash

echo "=================================="
echo "   Live Messaging Server"
echo "=================================="
echo ""

# Check if JAR exists
if [ ! -f "target/messaging-server.jar" ]; then
    echo "Server JAR not found. Building..."
    mvn clean package
    
    if [ $? -ne 0 ]; then
        echo "Build failed!"
        exit 1
    fi
fi

# Get port from argument or use default
PORT=${1:-5000}

echo "Starting server on port $PORT..."
echo ""

java -jar target/messaging-server.jar $PORT
