package com.gateway.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
public class OllamaClient {

    private final RestClient client;

    public OllamaClient(@Value("${ollama.base-url}") String baseUrl) {
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public String ask(String model, String prompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "prompt", prompt,
                "stream", false
        );

        Map resp = client.post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        if (resp == null) {
            log.warn("Empty response from Ollama for model={}", model);
            return null;
        }

        return (String) resp.get("response");
    }
}
