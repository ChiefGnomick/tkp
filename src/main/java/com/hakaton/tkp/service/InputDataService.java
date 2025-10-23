package com.hakaton.tkp.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hakaton.tkp.controller.EmbeddingController.EmbeddingRecord;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class InputDataService {
    private EmbeddingService embeddingService;
    private SaveRecordsService saveRecordsService;

    public ResponseEntity<?> parseFile(MultipartFile file){
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream()))){
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String id = UUID.randomUUID().toString();
                List<Byte> embedding = embeddingService.generateEmbedding(line);
                EmbeddingRecord record = new EmbeddingRecord(id, line, embedding);
                saveRecordsService.saveRecord(record, id);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }
}
