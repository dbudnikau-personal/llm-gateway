package com.gateway.service;

import com.gateway.client.OllamaClient;
import com.gateway.model.AssistantResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OllamaBackendTest {

    @Mock OllamaClient client;
    @Mock LocalModelResolver resolver;

    @InjectMocks OllamaBackend backend;

    @Test
    void canHandle_alwaysTrue() {
        assertThat(backend.canHandle("anything")).isTrue();
        assertThat(backend.canHandle("")).isTrue();
    }

    @Test
    void ask_resolvesModelAndReturnsWrappedResponse() {
        when(resolver.resolve("coder")).thenReturn("qwen2.5-coder:7b-instruct-q4_K_M");
        when(client.ask("qwen2.5-coder:7b-instruct-q4_K_M", "hi")).thenReturn("hello back");

        AssistantResponse result = backend.ask(Map.of("model", "coder"), "hi");

        assertThat(result.model()).isEqualTo("qwen2.5-coder:7b-instruct-q4_K_M");
        assertThat(result.content().get(0).text()).isEqualTo("hello back");
        assertThat(result.stopReason()).isEqualTo("end_turn");
    }

    @Test
    void ask_clientReturnsNull_responseIsEmpty() {
        when(resolver.resolve(null)).thenReturn("default-model");
        when(client.ask("default-model", "hi")).thenReturn(null);

        AssistantResponse result = backend.ask(Map.of(), "hi");

        assertThat(result.content().get(0).text()).isEmpty();
    }
}
