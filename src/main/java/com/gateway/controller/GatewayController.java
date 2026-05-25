package com.gateway.controller;

import com.gateway.client.AnthropicClient;
import com.gateway.client.OllamaClient;
import com.gateway.dto.AnthropicResponse;
import com.gateway.model.AssistantResponse;
import com.gateway.service.ComplexityAnalyzer;
import com.gateway.service.LocalModelResolver;
import com.gateway.service.MessageExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class GatewayController {

    private final OllamaClient ollamaClient;
    private final AnthropicClient anthropicClient;
    private final LocalModelResolver localModelResolver;
    private final MessageExtractor messageExtractor;
    private final ComplexityAnalyzer complexityAnalyzer;

    @Value("${gateway.auth.token:}")
    private String authToken;

    @SuppressWarnings("unchecked")
    @PostMapping("/messages")
    public ResponseEntity<?> messages(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> req) {

        if (!authToken.isBlank() && !("Bearer " + authToken).equals(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("type", "error", "error", Map.of(
                            "type", "authentication_error",
                            "message", "invalid api key")));
        }

        List<?> rawMessages = req.get("messages") instanceof List<?> l ? l : null;
        if (rawMessages == null || rawMessages.isEmpty()) {
            return ResponseEntity.ok(new AssistantResponse(localModelResolver.resolve(null), "empty messages"));
        }

        List<Map<String, Object>> messages = rawMessages.stream()
                .filter(m -> m instanceof Map)
                .map(m -> (Map<String, Object>) m)
                .toList();

        String prompt = messageExtractor.extractUserText(messages);

        if (anthropicClient.isEnabled() && complexityAnalyzer.isComplex(prompt)) {
            log.info("routing=claude prompt_len={}", prompt.length());
            AnthropicResponse response = anthropicClient.ask(req);
            return ResponseEntity.ok(response);
        }

        String model = localModelResolver.resolve((String) req.get("model"));
        log.info("routing=local model={} prompt_len={}", model, prompt.length());

        String answer = ollamaClient.ask(model, prompt);
        return ResponseEntity.ok(new AssistantResponse(model, answer == null ? "" : answer));
    }
}
