package com.hakaton.tkp.controller;

import com.hakaton.tkp.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/simple")
    public String sendPrompt(@RequestBody String prompt) {
        return chatService.sendPromptWithRAG(prompt, "");
    }

    @PostMapping("/rag")
    public String sendPromptWithRAG(@RequestBody RAGRequest request) {
        return chatService.sendPromptWithRAG(request.getPrompt(), request.getContext());
    }

    @PostMapping("/extract-json")
    public String extractJson(@RequestBody String llmResponse) {
        return chatService.extractJsonFromResponse(llmResponse);
    }

    @PostMapping("/rag-json")
    public String processWithRAGAndExtractJson(@RequestBody RAGRequest request) {
        return chatService.processPromptWithRAGAndExtractJson(request.getPrompt(), request.getContext());
    }

    public static class RAGRequest {
        private String prompt;
        private String context;

        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
    }
}