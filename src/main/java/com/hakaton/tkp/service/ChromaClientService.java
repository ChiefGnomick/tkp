package com.hakaton.tkp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ChromaClientService {
    
    private final WebClient chromaWebClient;
    private final EmbeddingService embeddingService;
    
    @Value("${app.chroma.collection-name:documents}")
    private String collectionName;
    
    // DTO классы внутри сервиса
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ChromaAddRequest {
        private List<String> ids;
        private List<List<Float>> embeddings;
        private List<String> documents;
        private List<Map<String, String>> metadatas;
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ChromaQueryRequest {
        private List<List<Float>> query_embeddings;
        private int n_results;
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ChromaQueryResponse {
        private List<List<String>> ids;
        private List<List<List<Float>>> embeddings;
        private List<List<String>> documents;
        private List<List<Map<String, String>>> metadatas;
        private List<List<Float>> distances;
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class SearchResult {
        private String document;
        private Float similarity;
        private Map<String, String> metadata;
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void initializeCollection() {
        createCollectionIfNotExists()
            .doOnSuccess(v -> System.out.println("Chroma collection '" + collectionName + "' is ready"))
            .doOnError(e -> System.err.println("Failed to initialize Chroma collection: " + e.getMessage()))
            .subscribe();
    }
    
    public Mono<Void> createCollectionIfNotExists() {
        Map<String, Object> body = Map.of(
            "name", collectionName,
            "get_or_create", true
        );
        
        return chromaWebClient
                .post()
                .uri("/api/v1/collections")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Object.class)
                .then();
    }
    
    public Mono<Void> addDocuments(List<String> documents, Map<String, String> metadata) {
        System.out.println("Adding " + documents.size() + " documents to Chroma");
        
        List<List<Float>> embeddings = embeddingService.generateEmbeddings(documents);
        
        ChromaAddRequest request = new ChromaAddRequest();
        request.setIds(generateIds(documents.size()));
        request.setDocuments(documents);
        request.setEmbeddings(embeddings);
        request.setMetadatas(Collections.nCopies(documents.size(), 
            metadata != null ? metadata : Map.of()));
        
        return chromaWebClient
                .post()
                .uri("/api/v1/collections/{collection_name}/add", collectionName)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Object.class)
                .doOnSuccess(v -> System.out.println("Successfully added " + documents.size() + " documents"))
                .doOnError(e -> System.err.println("Failed to add documents to Chroma: " + e.getMessage()))
                .then();
    }
    
    public Mono<List<SearchResult>> searchSimilar(String query, int limit) {
        System.out.println("Searching similar documents for query: '" + query + "'");
        
        List<Float> queryEmbedding = embeddingService.generateEmbedding(query);
        
        ChromaQueryRequest request = new ChromaQueryRequest();
        request.setQuery_embeddings(List.of(queryEmbedding));
        request.setN_results(limit);
        
        return chromaWebClient
                .post()
                .uri("/api/v1/collections/{collection_name}/query", collectionName)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChromaQueryResponse.class)
                .map(this::mapToSearchResults)
                .doOnSuccess(results -> System.out.println("Found " + results.size() + " similar documents"))
                .doOnError(e -> System.err.println("Search failed for query: '" + query + "': " + e.getMessage()));
    }
    
    public Mono<Long> getDocumentCount() {
        return chromaWebClient
                .get()
                .uri("/api/v1/collections/{collection_name}/count", collectionName)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> Long.valueOf(response.get("count").toString()));
    }
    
    private List<SearchResult> mapToSearchResults(ChromaQueryResponse response) {
        List<SearchResult> results = new ArrayList<>();
        
        if (response.getDocuments() != null && !response.getDocuments().isEmpty()) {
            List<String> documents = response.getDocuments().get(0);
            List<Float> distances = response.getDistances() != null && 
                                !response.getDistances().isEmpty() ? 
                                response.getDistances().get(0) : 
                                Collections.emptyList();
            
            List<Map<String, String>> metadatas = response.getMetadatas() != null && 
                                                !response.getMetadatas().isEmpty() ? 
                                                response.getMetadatas().get(0) : 
                                                Collections.emptyList();
            
            for (int i = 0; i < documents.size(); i++) {
                float similarity = distances.isEmpty() ? 1.0f : 1.0f - (distances.get(i) / 2.0f);
                
                Map<String, String> metadata = metadatas.size() > i ? 
                                            metadatas.get(i) : 
                                            Map.of();
                
                SearchResult result = new SearchResult(
                    documents.get(i),
                    similarity,
                    metadata
                );
                results.add(result);
            }
        }
        
        return results;
    }
    
    private List<String> generateIds(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> "doc_" + System.currentTimeMillis() + "_" + i)
                .collect(Collectors.toList());
    }
}