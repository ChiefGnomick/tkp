package com.hakaton.tkp.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hakaton.tkp.service.ChatService;
import com.hakaton.tkp.service.VectorSearchService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    
    private final VectorSearchService vectorSearchService;
    private final ChatService chatService;
    
    @PostMapping("/vector")
    public List<VectorSearchService.SearchResult> vectorSearch(
            @RequestBody SearchRequest request) {
        return vectorSearchService.searchSimilarDocuments(
            request.getQuery(), 
            request.getTopK()
        );
    }
    
    @PostMapping("/rag-context")
    public String getRAGContext(@RequestBody SearchRequest request) {
        return vectorSearchService.searchForRAG(
            request.getQuery(), 
            request.getTopK()
        );
    }
    
    @PostMapping("/chat-with-rag")
    public String chatWithRAG(@RequestBody RAGChatRequest request) {
        // Получаем контекст из векторного поиска
        String ragContext = vectorSearchService.searchForRAG(
            request.getQuery(), 
            request.getContextChunks()
        );
        
        // Отправляем запрос в LLM с контекстом
        return chatService.sendPromptWithRAG(request.getQuery(), ragContext);
    }
    
    @Data
    public static class SearchRequest {
        private String query;
        private int topK = 5;
        // геттеры и сеттеры
    }
    
    @Data
    public static class RAGChatRequest {
        private String query;
        private int contextChunks = 3;
        // геттеры и сеттеры
    }
}