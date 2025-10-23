package com.hakaton.tkp.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping("/simple")
    public String sendPrompt(@RequestBody String prompt) {
        log.info("Received prompt: {}", prompt);
        
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        
        log.info("AI Response: {}", response);
        return response;
    }
}