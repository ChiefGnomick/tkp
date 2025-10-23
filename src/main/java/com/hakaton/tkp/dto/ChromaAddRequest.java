package com.hakaton.tkp.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChromaAddRequest {
    private List<String> ids;
    private List<List<Float>> embeddings;
    private List<String> documents;
    private List<Map<String, String>> metadatas;
}
