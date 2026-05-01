package com.gateway.model;

import java.util.List;

public record AssistantResponse(
        String type,
        String role,
        List<Content> content
) {
    public AssistantResponse(String text) {
        this("message", "assistant", List.of(new Content("text", text)));
    }

    public record Content(String type, String text) {}
}
