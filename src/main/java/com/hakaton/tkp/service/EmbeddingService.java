package com.hakaton.tkp.service;

import java.util.List;

public interface EmbeddingService {
    
    /**
     * Генерирует эмбеддинг для одного текста
     * @param text исходный текст
     * @return вектор эмбеддинга
     */
    List<Float> generateEmbedding(String text);
    
    /**
     * Генерирует эмбеддинги для списка текстов
     * @param texts список текстов
     * @return список векторов эмбеддингов
     */
    List<List<Float>> generateEmbeddings(List<String> texts);
    
    /**
     * Возвращает размерность эмбеддингов
     * @return размерность вектора
     */
    int getEmbeddingDimension();
}