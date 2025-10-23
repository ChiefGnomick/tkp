package com.hakaton.tkp.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class MockEmbeddingService implements EmbeddingService {
    
    private final Random random = new Random();
    private final int embeddingDimension = 384;
    
    @Override
    public List<Float> generateEmbedding(String text) {
        System.out.println("Generating embedding for text: " + text.substring(0, Math.min(50, text.length())));
        return generateRandomVector(embeddingDimension);
    }
    
    @Override
    public List<List<Float>> generateEmbeddings(List<String> texts) {
        System.out.println("Generating embeddings for " + texts.size() + " texts");
        return texts.stream()
                .map(this::generateEmbedding)
                .collect(Collectors.toList());
    }
    
    @Override
    public int getEmbeddingDimension() {
        return embeddingDimension;
    }
    
    private List<Float> generateRandomVector(int dimension) {
        List<Float> vector = new ArrayList<>(dimension);
        for (int i = 0; i < dimension; i++) {
            vector.add(random.nextFloat() * 2 - 1);
        }
        return vector;
    }
}