package com.hakaton.tkp.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hakaton.tkp.service.ChatService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestController {

    private final ChatService chatService;

    @GetMapping("/send")
    public String getMethodName(@RequestParam String text) {

        return chatService.extractJsonFromResponse(chatService.sendPromptWithRAG(text));
    }

}
