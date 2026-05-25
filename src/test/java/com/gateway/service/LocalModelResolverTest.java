package com.gateway.service;

import com.gateway.config.ModelAliasProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LocalModelResolverTest {

    private static final String QWEN = "qwen2.5-coder:7b-instruct-q4_K_M";

    private final ModelAliasProperties aliasProps = new ModelAliasProperties();
    private final LocalModelResolver resolver = new LocalModelResolver(aliasProps);

    @BeforeEach
    void setUp() {
        aliasProps.setAliases(Map.of("coder", QWEN, "gemma4:31b-cloud", QWEN));
        ReflectionTestUtils.setField(resolver, "defaultModel", "default-model:latest");
    }

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
        assertThat(resolver.resolve("coder")).isEqualTo(QWEN);
    }

    @Test
    void cloudAlias_resolvesToQwen() {
        assertThat(resolver.resolve("gemma4:31b-cloud")).isEqualTo(QWEN);
    }
}
