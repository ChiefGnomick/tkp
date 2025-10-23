package com.hakaton.tkp.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String prompt;
    private String description;
    private String jsonSchema;
}