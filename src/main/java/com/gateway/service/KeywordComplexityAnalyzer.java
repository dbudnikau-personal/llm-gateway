package com.gateway.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class KeywordComplexityAnalyzer implements ComplexityAnalyzer {

    private static final Set<String> COMPLEX_SIGNALS = Set.of(
            "explain", "analyze", "analyse", "refactor", "architecture",
            "why does", "why is", "how does", "how do", "what's wrong",
            "review", "compare", "debug", "design", "describe"
    );

    private final int threshold;

    public KeywordComplexityAnalyzer(@Value("${gateway.routing.complexity-threshold}") int threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean isComplex(String text) {
        if (text.length() > threshold) {
            return true;
        }
        String lower = text.toLowerCase();
        return COMPLEX_SIGNALS.stream().anyMatch(lower::contains);
    }
}
