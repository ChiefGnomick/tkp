package com.hakaton.tkp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hakaton.tkp.dto.AddDocumentsRequest;
import com.hakaton.tkp.dto.SearchRequest;
import com.hakaton.tkp.service.ChromaClientService;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@Validated
public class DocumentController {
    
    private final ChromaClientService chromaService;
    
    public DocumentController(ChromaClientService chromaService) {
        this.chromaService = chromaService;
    }
    
    @PostMapping("/add")
    public Mono<ResponseEntity<Map<String, Object>>> addDocuments(
            @Valid @RequestBody AddDocumentsRequest request) {
        
        return chromaService.addDocuments(request.getDocuments(), request.getMetadata())
                .then(Mono.fromCallable(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "Documents added successfully");
                    response.put("count", request.getDocuments().size());
                    return ResponseEntity.ok(response);
                }))
                .onErrorResume(e -> Mono.fromCallable(() -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("status", "error");
                    errorResponse.put("message", "Failed to add documents: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                }));
    }
    
    @PostMapping("/search")
    public Mono<ResponseEntity<Map<String, Object>>> searchSimilar(
            @Valid @RequestBody SearchRequest request) {
        
        return chromaService.searchSimilar(request.getQuery(), request.getLimit())
                .map(results -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("query", request.getQuery());
                    response.put("results", results);
                    response.put("count", results.size());
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> Mono.fromCallable(() -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("status", "error");
                    errorResponse.put("message", "Search failed: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                }));
    }
    
    @GetMapping("/count")
    public Mono<ResponseEntity<Map<String, Object>>> getDocumentCount() {
        return chromaService.getDocumentCount()
                .map(count -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("count", count);
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> Mono.fromCallable(() -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("status", "error");
                    errorResponse.put("message", "Failed to get document count");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                }));
    }
    
    @PostMapping("/collection/init")
    public Mono<ResponseEntity<Map<String, Object>>> initializeCollection() {
        return chromaService.createCollectionIfNotExists()
                .then(Mono.fromCallable(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "Collection initialized successfully");
                    return ResponseEntity.ok(response);
                }));
    }
}