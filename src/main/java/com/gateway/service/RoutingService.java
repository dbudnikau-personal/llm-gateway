package com.gateway.service;

import com.gateway.model.AssistantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoutingService {

    private final List<LlmBackend> backends;

    public AssistantResponse route(Map<String, Object> req, String prompt) {
        return backends.stream()
                .filter(b -> b.canHandle(prompt))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No LLM backend available"))
                .ask(req, prompt);
    }
}
