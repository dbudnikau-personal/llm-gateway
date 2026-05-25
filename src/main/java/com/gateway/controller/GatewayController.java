package com.gateway.controller;

import com.gateway.service.MessageExtractor;
import com.gateway.service.RoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class GatewayController {

    private final RoutingService routingService;
    private final MessageExtractor messageExtractor;

    @Value("${gateway.auth.token:}")
    private String authToken;

    @SuppressWarnings("unchecked")
    @PostMapping("/messages")
    public ResponseEntity<?> messages(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> req) {

        if (!authToken.isBlank() && !("Bearer " + authToken).equals(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("type", "error", "error", Map.of(
                            "type", "authentication_error",
                            "message", "invalid api key")));
        }

        List<?> rawMessages = req.get("messages") instanceof List<?> l ? l : null;
        if (rawMessages == null || rawMessages.isEmpty()) {
            return ResponseEntity.ok(routingService.route(req, ""));
        }

        List<Map<String, Object>> messages = rawMessages.stream()
                .filter(m -> m instanceof Map)
                .map(m -> (Map<String, Object>) m)
                .toList();

        String prompt = messageExtractor.extractUserText(messages);
        return ResponseEntity.ok(routingService.route(req, prompt));
    }
}
