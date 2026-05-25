package com.gateway.service;

import com.gateway.model.AssistantResponse;

import java.util.Map;

public interface LlmBackend {
    boolean canHandle(String prompt);
    AssistantResponse ask(Map<String, Object> req, String prompt);
}
