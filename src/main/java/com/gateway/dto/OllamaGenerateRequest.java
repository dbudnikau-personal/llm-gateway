package com.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OllamaGenerateRequest(
        String model,
        String prompt,
        boolean stream
) {}
