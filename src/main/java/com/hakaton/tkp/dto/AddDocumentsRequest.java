package com.hakaton.tkp.dto;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddDocumentsRequest {
    @NotEmpty
    private List<String> documents;

    private Map<String, String> metadata;
}
