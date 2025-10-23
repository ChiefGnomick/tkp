package com.hakaton.tkp.service;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;
    private Integer embeddingDimension = null;

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public List<Float> generateEmbedding(String text) {
        EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));
        float[] embeddingArray = response.getResults().get(0).getOutput();
        if (embeddingDimension == null) {
            embeddingDimension = embeddingArray.length;
        }
        return IntStream.range(0, embeddingArray.length)
                        .mapToObj(i -> embeddingArray[i])
                        .collect(Collectors.toList());
    }

    public List<List<Float>> generateEmbeddings(List<String> texts) {
        EmbeddingResponse response = embeddingModel.embedForResponse(texts);
        
        return response.getResults().stream()
                .map(embedding -> {
                    float[] embeddingArray = embedding.getOutput();
                    if (embeddingDimension == null) {
                        embeddingDimension = embeddingArray.length;
                    }
                    return IntStream.range(0, embeddingArray.length)
                                    .mapToObj(i -> embeddingArray[i])
                                    .collect(Collectors.toList());
                })
                .collect(Collectors.toList());
    }

    public int getEmbeddingDimension() {
        if (embeddingDimension == null) {
            List<Float> testEmbedding = generateEmbedding("test");
            embeddingDimension = testEmbedding.size();
        }
        return embeddingDimension;
    }
}