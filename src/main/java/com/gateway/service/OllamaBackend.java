package com.gateway.service;

import com.gateway.client.OllamaClient;
import com.gateway.model.AssistantResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@Order(2)
@RequiredArgsConstructor
public class OllamaBackend implements LlmBackend {

    private final OllamaClient client;
    private final LocalModelResolver resolver;

    @Override
    public boolean canHandle(String prompt) {
        return true;
    }

    @Override
    public AssistantResponse ask(Map<String, Object> req, String prompt) {
        String model = resolver.resolve((String) req.get("model"));
        log.info("routing=local model={} prompt_len={}", model, prompt.length());
        String answer = client.ask(model, prompt);
        return new AssistantResponse(model, answer == null ? "" : answer);
    }
}
