package com.gateway.client;

import com.gateway.dto.AnthropicResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class AnthropicClient {

    private static final Set<String> ALLOWED_FIELDS = Set.of(
            "messages", "system", "max_tokens", "temperature",
            "stop_sequences", "top_p", "top_k", "metadata", "stream"
    );

    private final RestClient client;
    private final String model;
    private final boolean enabled;

    public AnthropicClient(
            @Value("${anthropic.base-url}") String baseUrl,
            @Value("${anthropic.model}") String model,
            @Value("${anthropic.api-key:}") String apiKey) {
        this.model = model;
        this.enabled = !apiKey.isBlank();
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public AnthropicResponse ask(Map<String, Object> originalReq) {
        Map<String, Object> body = new HashMap<>();
        ALLOWED_FIELDS.forEach(field -> {
            if (originalReq.containsKey(field)) {
                body.put(field, originalReq.get(field));
            }
        });
        body.put("model", model);
        body.putIfAbsent("max_tokens", 8096);

        log.debug("forwarding to Anthropic model={}", model);

        return client.post()
                .uri("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(AnthropicResponse.class);
    }
}
