package com.hakaton.tkp.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GenerationService {

    private ChatClient chatClient;

    public void test() {
        
    }

}