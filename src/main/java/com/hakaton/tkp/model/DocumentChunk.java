package com.hakaton.tkp.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class DocumentChunk {
    private String id;
    private String documentId;
    private String content;
    private List<Float> embedding;
    private Map<String, String> metadata;
    private LocalDateTime createdAt;
    
    public DocumentChunk(String content) {
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}