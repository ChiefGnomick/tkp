package com.hakaton.tkp.controller;

import com.hakaton.tkp.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/embedding")
@RequiredArgsConstructor
public class EmbeddingController {

    private final EmbeddingService embeddingService;
    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/add")
    public String addTextEmbedding(@RequestParam String text) {
        // 1. Генерируем эмбеддинг
        List<Float> embedding = embeddingService.generateEmbedding(text);

        // 2. Формируем уникальный ключ
        String id = UUID.randomUUID().toString();

        // 3. Сохраняем в Redis
        redisTemplate.opsForHash().put("embeddings", id, new EmbeddingRecord(id, text, embedding));

        return "✅ Добавлено: " + id;
    }

    @GetMapping("/all")
    public Object getAllEmbeddings() {
        return redisTemplate.opsForHash().values("embeddings");
    }

    public record EmbeddingRecord(String id, String text, List<Float> embedding) {}
}
