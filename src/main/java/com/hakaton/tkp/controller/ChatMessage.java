package com.hakaton.tkp.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage {
    private String sender;
    private String message;
    private String timestamp;

    public ChatMessage() {
        this.timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public ChatMessage(String sender, String message) {
        this();
        this.sender = sender;
        this.message = message;
    }

    // Геттеры и сеттеры
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}