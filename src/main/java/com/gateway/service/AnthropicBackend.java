package com.gateway.service;

import com.gateway.client.AnthropicClient;
import com.gateway.model.AssistantResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@Order(1)
@RequiredArgsConstructor
public class AnthropicBackend implements LlmBackend {

    private final AnthropicClient client;
    private final ComplexityAnalyzer complexityAnalyzer;

    @Override
    public boolean canHandle(String prompt) {
        return client.isEnabled() && complexityAnalyzer.isComplex(prompt);
    }

    @Override
    public AssistantResponse ask(Map<String, Object> req, String prompt) {
        log.info("routing=claude prompt_len={}", prompt.length());
        return client.ask(req);
    }
}
