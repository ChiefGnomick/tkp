package com.hakaton.tkp.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.hakaton.tkp.controller.EmbeddingController.EmbeddingRecord;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaveRecordsService {
    private final RedisTemplate<String, Object> redisTemplate;

    public void saveRecord(EmbeddingRecord record, String id){
        redisTemplate.opsForHash().put("embeddings", id, record);
    }
}
