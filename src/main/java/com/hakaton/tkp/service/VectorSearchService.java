package com.hakaton.tkp.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VectorSearchService {

    private final RedisTemplate<String, String> redisTemplate;
    private final EmbeddingService embeddingService;
    
    // Ключ для хранения векторного индекса в Redis
    private static final String VECTOR_INDEX_NAME = "document_vectors";
    private static final String TEXT_PREFIX = "text:";
    private static final String VECTOR_PREFIX = "vector:";

    public VectorSearchService(RedisTemplate<String, String> redisTemplate, 
                             EmbeddingService embeddingService) {
        this.redisTemplate = redisTemplate;
        this.embeddingService = embeddingService;
    }

    /**
     * Поиск похожих документов по текстовому запросу
     */
    public List<SearchResult> searchSimilarDocuments(String query, int topK) {
        log.info("Searching similar documents for query: {}", query);
        
        // Генерируем эмбеддинг для запроса
        List<Float> queryEmbedding = embeddingService.generateEmbedding(query);
        byte[] queryVector = convertToByteArray(queryEmbedding);
        
        // Выполняем поиск в Redis
        return performVectorSearch(queryVector, topK);
    }

    /**
     * Поиск похожих документов по готовому эмбеддингу
     */
    public List<SearchResult> searchSimilarDocuments(List<Float> queryEmbedding, int topK) {
        log.info("Searching similar documents with provided embedding");
        
        byte[] queryVector = convertToByteArray(queryEmbedding);
        return performVectorSearch(queryVector, topK);
    }

    /**
     * Полнотекстовый поиск с последующим векторным поиском
     */
    public List<SearchResult> hybridSearch(String query, int topK) {
        log.info("Performing hybrid search for query: {}", query);
        
        // Сначала выполняем полнотекстовый поиск (простой пример)
        List<String> textResults = performTextSearch(query);
        
        if (textResults.isEmpty()) {
            // Если полнотекстовый поиск не дал результатов, используем векторный
            return searchSimilarDocuments(query, topK);
        }
        
        // Комбинируем результаты
        List<SearchResult> allResults = new ArrayList<>();
        
        // Добавляем результаты полнотекстового поиска
        for (String text : textResults) {
            allResults.add(new SearchResult(text, 1.0, "text_search"));
        }
        
        // Добавляем результаты векторного поиска
        List<SearchResult> vectorResults = searchSimilarDocuments(query, topK);
        allResults.addAll(vectorResults);
        
        // Сортируем по релевантности и ограничиваем количество
        return allResults.stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(topK)
                .collect(Collectors.toList());
    }

    /**
     * Получение текста по ID документа
     */
    public String getDocumentText(String documentId) {
        return redisTemplate.opsForValue().get(TEXT_PREFIX + documentId);
    }

    /**
     * Получение нескольких текстов по ID документов
     */
    public Map<String, String> getDocumentsTexts(List<String> documentIds) {
        Map<String, String> result = new HashMap<>();
        
        for (String docId : documentIds) {
            String text = getDocumentText(docId);
            if (text != null) {
                result.put(docId, text);
            }
        }
        
        return result;
    }

    /**
     * Поиск для RAG - возвращает наиболее релевантные тексты для контекста
     */
    public String searchForRAG(String query, int contextChunks) {
        log.info("Searching for RAG context for query: {}", query);
        
        List<SearchResult> searchResults = searchSimilarDocuments(query, contextChunks);
        
        // Объединяем наиболее релевантные тексты в контекст
        return searchResults.stream()
                .map(SearchResult::getText)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n\n"));
    }

    private List<SearchResult> performVectorSearch(byte[] queryVector, int topK) {
        List<SearchResult> results = new ArrayList<>();
        
        try {
            // Используем Redis команды для векторного поиска
            // Для Redis Stack с модулем поиска
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            
            // Пример поиска с использованием FT.SEARCH (RediSearch)
            // В реальной реализации нужно настроить индекс в Redis
            String searchCommand = buildVectorSearchCommand(queryVector, topK);
            
            // Здесь должна быть реализация поиска через RediSearch
            // Это упрощенный пример - в реальности нужно использовать RedisSearchTemplate
            // или низкоуровневые команды
            
            // Временная реализация - поиск по всем ключам с вычислением косинусного сходства
            results = fallbackVectorSearch(queryVector, topK);
            
        } catch (Exception e) {
            log.error("Error during vector search", e);
            results = fallbackVectorSearch(queryVector, topK);
        }
        
        return results;
    }

    /**
     * Резервный метод поиска - вычисляет сходство со всеми векторами в БД
     */
    private List<SearchResult> fallbackVectorSearch(byte[] queryVector, int topK) {
        List<SearchResult> results = new ArrayList<>();
        List<Float> queryEmbedding = convertToFloatList(queryVector);
        
        // Получаем все ключи векторов
        Set<String> vectorKeys = redisTemplate.keys(VECTOR_PREFIX + "*");
        
        if (vectorKeys == null || vectorKeys.isEmpty()) {
            log.warn("No vectors found in database");
            return results;
        }
        
        for (String vectorKey : vectorKeys) {
            try {
                // Получаем вектор из Redis
                String vectorData = redisTemplate.opsForValue().get(vectorKey);
                if (vectorData != null) {
                    // Извлекаем ID документа
                    String docId = vectorKey.substring(VECTOR_PREFIX.length());
                    
                    // Получаем текст документа
                    String text = getDocumentText(docId);
                    
                    // Преобразуем строку с вектором обратно в List<Float>
                    List<Float> docEmbedding = parseEmbeddingString(vectorData);
                    
                    // Вычисляем косинусное сходство
                    double similarity = cosineSimilarity(queryEmbedding, docEmbedding);
                    
                    results.add(new SearchResult(text, similarity, docId));
                }
            } catch (Exception e) {
                log.warn("Error processing vector: {}", vectorKey, e);
            }
        }
        
        // Сортируем по убыванию сходства и ограничиваем количество
        return results.stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(topK)
                .collect(Collectors.toList());
    }

    private List<String> performTextSearch(String query) {
        // Простая реализация полнотекстового поиска
        // В реальной системе лучше использовать RediSearch
        Set<String> textKeys = redisTemplate.keys(TEXT_PREFIX + "*");
        List<String> results = new ArrayList<>();
        
        if (textKeys != null) {
            for (String key : textKeys) {
                String text = redisTemplate.opsForValue().get(key);
                if (text != null && text.toLowerCase().contains(query.toLowerCase())) {
                    results.add(text);
                }
            }
        }
        
        return results;
    }

    private String buildVectorSearchCommand(byte[] queryVector, int topK) {
        // Строим команду для векторного поиска в Redis
        // Это пример - реальная реализация зависит от конфигурации Redis Search
        return String.format("FT.SEARCH %s \"*=>[KNN %d @vector $query_vector]\" " +
                           "PARAMS 2 query_vector %s DIALECT 2", 
                           VECTOR_INDEX_NAME, topK, Base64.getEncoder().encodeToString(queryVector));
    }

    private byte[] convertToByteArray(List<Float> embedding) {
        ByteBuffer buffer = ByteBuffer.allocate(embedding.size() * Float.BYTES);
        embedding.forEach(buffer::putFloat);
        return buffer.array();
    }

    private List<Float> convertToFloatList(byte[] vector) {
        List<Float> result = new ArrayList<>();
        ByteBuffer buffer = ByteBuffer.wrap(vector);
        
        while (buffer.hasRemaining()) {
            result.add(buffer.getFloat());
        }
        
        return result;
    }

    private List<Float> parseEmbeddingString(String embeddingString) {
        // Предполагаем, что эмбеддинг хранится как строка чисел, разделенных запятыми
        return Arrays.stream(embeddingString.split(","))
                .map(Float::parseFloat)
                .collect(Collectors.toList());
    }

    private double cosineSimilarity(List<Float> vectorA, List<Float> vectorB) {
        if (vectorA.size() != vectorB.size()) {
            throw new IllegalArgumentException("Vectors must have same dimension");
        }
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }
        
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Класс для хранения результатов поиска
     */
    public static class SearchResult {
        private final String text;
        private final double score;
        private final String documentId;
        
        public SearchResult(String text, double score, String documentId) {
            this.text = text;
            this.score = score;
            this.documentId = documentId;
        }
        
        // Геттеры
        public String getText() { return text; }
        public double getScore() { return score; }
        public String getDocumentId() { return documentId; }
        
        @Override
        public String toString() {
            return String.format("SearchResult{score=%.4f, documentId='%s', text='%s'}", 
                               score, documentId, 
                               text != null ? text.substring(0, Math.min(50, text.length())) + "..." : "null");
        }
    }
}