package com.gateway.controller;

import com.gateway.client.AnthropicClient;
import com.gateway.client.OllamaClient;
import com.gateway.model.AssistantResponse;
import com.gateway.service.ComplexityAnalyzer;
import com.gateway.service.MessageExtractor;
import com.gateway.service.ModelRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class GatewayController {

    private final OllamaClient ollamaClient;
    private final AnthropicClient anthropicClient;
    private final ModelRouter modelRouter;
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
            return ResponseEntity.ok(new AssistantResponse("empty messages"));
        }

        List<Map<String, Object>> messages = rawMessages.stream()
                .filter(m -> m instanceof Map)
                .map(m -> (Map<String, Object>) m)
                .toList();

        String prompt = messageExtractor.extractUserText(messages);

        if (anthropicClient.isEnabled() && complexityAnalyzer.isComplex(prompt)) {
            log.info("routing=claude prompt_len={}", prompt.length());
            Map<String, Object> response = anthropicClient.ask(req);
            return ResponseEntity.ok(response);
        }

        String model = modelRouter.resolve((String) req.get("model"));
        log.info("routing=local model={} prompt_len={}", model, prompt.length());

        String answer = ollamaClient.ask(model, prompt);
        String safeAnswer = answer == null ? "" : answer;

        Map<String, Object> text = new HashMap<>();
        text.put("type", "text");
        text.put("text", safeAnswer);

        Map<String, Object> usage = new HashMap<>();
        usage.put("input_tokens", 0);
        usage.put("output_tokens", 0);

        Map<String, Object> msg = new HashMap<>();
        msg.put("id", "msg_" + UUID.randomUUID().toString().replace("-", ""));
        msg.put("type", "message");
        msg.put("role", "assistant");
        msg.put("model", model);
        msg.put("content", List.of(text));
        msg.put("stop_reason", "end_turn");
        msg.put("stop_sequence", null);
        msg.put("usage", usage);

        return ResponseEntity.ok(msg);
    }
}
