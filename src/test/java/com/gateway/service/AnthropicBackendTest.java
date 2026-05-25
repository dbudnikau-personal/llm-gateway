package com.gateway.service;

import com.gateway.client.AnthropicClient;
import com.gateway.model.AssistantResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnthropicBackendTest {

    @Mock AnthropicClient client;
    @Mock ComplexityAnalyzer complexityAnalyzer;

    @InjectMocks AnthropicBackend backend;

    @Test
    void canHandle_clientEnabledAndComplexPrompt_returnsTrue() {
        when(client.isEnabled()).thenReturn(true);
        when(complexityAnalyzer.isComplex("explain this")).thenReturn(true);

        assertThat(backend.canHandle("explain this")).isTrue();
    }

    @Test
    void canHandle_clientDisabled_returnsFalse() {
        when(client.isEnabled()).thenReturn(false);

        assertThat(backend.canHandle("explain this")).isFalse();
    }

    @Test
    void canHandle_clientEnabledButSimplePrompt_returnsFalse() {
        when(client.isEnabled()).thenReturn(true);
        when(complexityAnalyzer.isComplex("hi")).thenReturn(false);

        assertThat(backend.canHandle("hi")).isFalse();
    }

    @Test
    void ask_delegatesToClientAndReturnsResponse() {
        AssistantResponse expected = new AssistantResponse("claude-sonnet-4-6", "deep answer");
        when(client.ask(any())).thenReturn(expected);

        Map<String, Object> req = Map.of("messages", List.of());
        AssistantResponse result = backend.ask(req, "explain this");

        assertThat(result).isSameAs(expected);
    }
}
