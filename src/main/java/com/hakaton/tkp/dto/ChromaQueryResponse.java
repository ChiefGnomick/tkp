package com.hakaton.tkp.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
class ChromaQueryResponse {
    private List<List<String>> ids;
    private List<List<List<Float>>> embeddings;
    private List<List<String>> documents;
    private List<List<Map<String, String>>> metadatas;
    private List<List<Float>> distances;
}