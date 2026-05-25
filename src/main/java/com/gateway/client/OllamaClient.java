package com.gateway.client;

import com.gateway.dto.OllamaGenerateRequest;
import com.gateway.dto.OllamaGenerateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class OllamaClient {

    private final RestClient client;

    @Autowired
    public OllamaClient(@Value("${ollama.base-url}") String baseUrl) {
        this(RestClient.builder()
                .requestFactory(new SimpleClientHttpRequestFactory())
                .baseUrl(baseUrl)
                .build());
    }

    OllamaClient(RestClient client) {
        this.client = client;
    }

    public String ask(String model, String prompt) {
        OllamaGenerateRequest body = new OllamaGenerateRequest(model, prompt, false);

        OllamaGenerateResponse resp = client.post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(OllamaGenerateResponse.class);

        if (resp == null) {
            log.warn("Empty response from Ollama for model={}", model);
            return null;
        }

        return resp.response();
    }
}
