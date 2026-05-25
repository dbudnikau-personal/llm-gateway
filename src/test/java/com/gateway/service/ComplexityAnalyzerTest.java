package com.gateway.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ComplexityAnalyzerTest {

    private final ComplexityAnalyzer analyzer = new ComplexityAnalyzer(500);

    @Test
    void shortSimplePrompt_isNotComplex() {
        assertThat(analyzer.isComplex("hello world")).isFalse();
    }

    @Test
    void promptExceedingThreshold_isComplex() {
        String longPrompt = "x".repeat(501);
        assertThat(analyzer.isComplex(longPrompt)).isTrue();
    }

    @Test
    void promptExactlyAtThreshold_isNotComplex() {
        String borderPrompt = "x".repeat(500);
        assertThat(analyzer.isComplex(borderPrompt)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "explain this code", "analyze the issue", "analyse this",
            "refactor the method", "review my PR", "compare these two",
            "debug the crash", "design a system", "describe the architecture",
            "why does this fail", "why is this slow", "how does it work", "how do I fix this",
            "what's wrong here"
    })
    void complexKeyword_isComplex(String prompt) {
        assertThat(analyzer.isComplex(prompt)).isTrue();
    }

    @Test
    void keywordsAreCaseInsensitive() {
        assertThat(analyzer.isComplex("EXPLAIN this")).isTrue();
        assertThat(analyzer.isComplex("Please ANALYZE")).isTrue();
    }
}
