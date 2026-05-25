package com.gateway.service;

import com.gateway.config.ModelAliasProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocalModelResolver {

    private final ModelAliasProperties aliasProperties;

    @Value("${ollama.model.default}")
    private String defaultModel;

    public String resolve(String requestedModel) {
        if (requestedModel == null || requestedModel.isBlank()) {
            return defaultModel;
        }
        return aliasProperties.getAliases().getOrDefault(requestedModel, defaultModel);
    }
}
