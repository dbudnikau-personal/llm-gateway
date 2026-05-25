package com.gateway.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AssistantResponse(
        String id,
        String type,
        String role,
        String model,
        List<Content> content,
        @JsonProperty("stop_reason") String stopReason,
        @JsonProperty("stop_sequence") String stopSequence,
        Usage usage
) {
    public AssistantResponse(String model, String text) {
        this(
                "msg_" + UUID.randomUUID().toString().replace("-", ""),
                "message",
                "assistant",
                model,
                List.of(new Content("text", text)),
                "end_turn",
                null,
                new Usage(0, 0)
        );
    }

    public record Content(String type, String text) {}

    public record Usage(
            @JsonProperty("input_tokens") int inputTokens,
            @JsonProperty("output_tokens") int outputTokens
    ) {}
}
