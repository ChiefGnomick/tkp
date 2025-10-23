package com.hakaton.tkp.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${app.chroma.url:http://localhost:8000}")
    private String chromaUrl;

    @Bean
    public WebClient chromaWebClient() {
        return WebClient.builder()
                .baseUrl(chromaUrl)
                .build();
    }
}
