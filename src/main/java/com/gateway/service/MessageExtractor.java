package com.gateway.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MessageExtractor {

    public String extractUserText(List<Map<String, Object>> messages) {
        StringBuilder sb = new StringBuilder();

        for (Map<String, Object> msg : messages) {
            if (!"user".equals(msg.get("role"))) {
                continue;
            }

            Object contentObj = msg.get("content");
            if (contentObj instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof Map<?, ?> map) {
                        String type = (String) map.get("type");
                        String text = (String) map.get("text");
                        if ("text".equals(type) && text != null) {
                            sb.append(text).append("\n");
                        }
                    }
                }
            }
        }

        return sb.toString().trim();
    }
}
