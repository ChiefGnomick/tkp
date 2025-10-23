package com.hakaton.tkp.controller;

import com.hakaton.tkp.service.EmbeddingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/embedding")
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    public EmbeddingController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @GetMapping("/single")
    public Map<String, Object> generateEmbedding(@RequestParam String text) {
        List<Float> embedding = embeddingService.generateEmbedding(text);
        
        return Map.of(
            "text", text,
            "embedding_size", embedding.size(),
            "embedding_sample", embedding.subList(0, Math.min(5, embedding.size())),
            "message", "Эмбеддинг успешно создан"
        );
    }

    @PostMapping("/batch")
    public Map<String, Object> generateEmbeddings(@RequestBody Map<String, List<String>> request) {
        List<String> texts = request.get("texts");
        List<List<Float>> embeddings = embeddingService.generateEmbeddings(texts);
        
        return Map.of(
            "texts_count", texts.size(),
            "embeddings_count", embeddings.size(),
            "embedding_dimension", embeddingService.getEmbeddingDimension(),
            "embeddings", embeddings
        );
    }

    @GetMapping("/dimension")
    public Map<String, Object> getDimension() {
        return Map.of(
            "embedding_dimension", embeddingService.getEmbeddingDimension()
        );
    }
}