#!/bin/bash

echo "=================================="
echo "   Live Messaging Client"
echo "=================================="
echo ""

# Check if JAR exists
if [ ! -f "target/messaging-client.jar" ]; then
    echo "Client JAR not found. Building..."
    mvn clean package
    
    if [ $? -ne 0 ]; then
        echo "Build failed!"
        exit 1
    fi
fi

echo "Starting client..."
echo ""

java -jar target/messaging-client.jar
