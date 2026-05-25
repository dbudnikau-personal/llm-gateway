package com.gateway.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MessageExtractorTest {

    private final MessageExtractor extractor = new MessageExtractor();

    @Test
    void extractsTextFromStringContent() {
        List<Map<String, Object>> messages = List.of(
                Map.of("role", "user", "content", List.of(
                        Map.of("type", "text", "text", "hello")
                ))
        );
        assertThat(extractor.extractUserText(messages)).isEqualTo("hello");
    }

    @Test
    void skipsAssistantMessages() {
        List<Map<String, Object>> messages = List.of(
                Map.of("role", "assistant", "content", List.of(
                        Map.of("type", "text", "text", "I am assistant")
                )),
                Map.of("role", "user", "content", List.of(
                        Map.of("type", "text", "text", "user message")
                ))
        );
        assertThat(extractor.extractUserText(messages)).isEqualTo("user message");
    }

    @Test
    void skipsNonTextBlocks() {
        List<Map<String, Object>> messages = List.of(
                Map.of("role", "user", "content", List.of(
                        Map.of("type", "image", "source", Map.of("url", "https://example.com/img.png")),
                        Map.of("type", "text", "text", "describe the image")
                ))
        );
        assertThat(extractor.extractUserText(messages)).isEqualTo("describe the image");
    }

    @Test
    void concatenatesMultipleUserTurns() {
        List<Map<String, Object>> messages = List.of(
                Map.of("role", "user", "content", List.of(Map.of("type", "text", "text", "first"))),
                Map.of("role", "assistant", "content", List.of(Map.of("type", "text", "text", "reply"))),
                Map.of("role", "user", "content", List.of(Map.of("type", "text", "text", "second")))
        );
        assertThat(extractor.extractUserText(messages)).isEqualTo("first\nsecond");
    }

    @Test
    void emptyMessages_returnsEmptyString() {
        assertThat(extractor.extractUserText(List.of())).isEmpty();
    }

    @Test
    void textBlockWithNullText_isSkipped() {
        List<Map<String, Object>> messages = List.of(
                Map.of("role", "user", "content", List.of(
                        Map.of("type", "text")
                ))
        );
        assertThat(extractor.extractUserText(messages)).isEmpty();
    }
}
