package com.gateway.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocalModelResolverTest {

    private final LocalModelResolver resolver = new LocalModelResolver("default-model:latest");

    @Test
    void nullModel_returnsDefault() {
        assertThat(resolver.resolve(null)).isEqualTo("default-model:latest");
    }

    @Test
    void blankModel_returnsDefault() {
        assertThat(resolver.resolve("  ")).isEqualTo("default-model:latest");
    }

    @Test
    void unknownModel_returnsDefault() {
        assertThat(resolver.resolve("gpt-4")).isEqualTo("default-model:latest");
    }

    @Test
    void coderAlias_resolvesToQwen() {
        assertThat(resolver.resolve("coder")).isEqualTo("qwen2.5-coder:7b-instruct-q4_K_M");
    }

    @Test
    void cloudAlias_resolvesToQwen() {
        assertThat(resolver.resolve("gemma4:31b-cloud")).isEqualTo("qwen2.5-coder:7b-instruct-q4_K_M");
    }
}
