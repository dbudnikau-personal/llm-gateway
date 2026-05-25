package com.gateway.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LocalModelResolver {

    private static final Map<String, String> ALIASES = Map.of(
            "gemma4:31b-cloud", "qwen2.5-coder:7b-instruct-q4_K_M",
            "coder", "qwen2.5-coder:7b-instruct-q4_K_M"
    );

    private final String defaultModel;

    public LocalModelResolver(@Value("${ollama.model.default}") String defaultModel) {
        this.defaultModel = defaultModel;
    }

    public String resolve(String requestedModel) {
        if (requestedModel == null || requestedModel.isBlank()) {
            return defaultModel;
        }
        return ALIASES.getOrDefault(requestedModel, defaultModel);
    }
}
