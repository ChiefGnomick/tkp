package com.hakaton.tkp.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final MaterialClient materialClient;

    public String sendPromptWithRAG(String userPrompt) {
        String ragContext = materialClient.searchMaterials(userPrompt, 3).toString();

        log.info("Received user prompt: {}", userPrompt);
        log.info("RAG context: {}", ragContext);
        
        String fullPrompt = buildRAGPrompt(userPrompt, ragContext);
        
        String response = chatClient.prompt()
                .user(fullPrompt)
                .call()
                .content();
        
        log.info("AI Response with RAG: {}", response);
        return response;
    }

    public String extractJsonFromResponse(String llmResponse) {
        log.info("Extracting JSON from response: {}", llmResponse);
        
        String jsonExtractionPrompt = buildJsonExtractionPrompt(llmResponse);
        
        String response = chatClient.prompt()
                .user(jsonExtractionPrompt)
                .call()
                .content();
        
        log.info("JSON Extraction Result: {}", response);
        return response;
    }

    private String buildRAGPrompt(String userPrompt, String ragContext) {
        return String.format("""
            Контекст для ответа:
            %s
            
            Вопрос пользователя: %s
            
            Ответь на вопрос пользователя, используя предоставленный контекст. 
            Если в контексте нет достаточной информации, сообщи об этом.
            """, ragContext, userPrompt);
    }

    private String buildJsonExtractionPrompt(String llmResponse) {
        return String.format("""
            Сообщение: %s
            
            Тебе нужно найти в сообщении JSON. Если его нет, то создать свой, взяв материалы, количество и стоимость.
            
            Требования к JSON:
            - Если находишь JSON в сообщении - верни его как есть
            - Если JSON нет - создай структуру на основе упомянутых материалов
            - В JSON должны быть поля: materials (массив объектов), quantities, prices
            - Верни ТОЛЬКО JSON без дополнительных объяснений
            """, llmResponse);
    }

    public String processPromptWithRAGAndExtractJson(String userPrompt) {
        String llmResponse = sendPromptWithRAG(userPrompt);
        return extractJsonFromResponse(llmResponse);
    }
}