package com.gateway.controller;

import com.gateway.service.RoutingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {

    @MockBean RoutingService routingService;

    @Autowired MockMvc mvc;

    @Test
    void backendThrowsRestClientException_returns502() throws Exception {
        when(routingService.route(any(), anyString()))
                .thenThrow(new ResourceAccessException("connection refused"));

        mvc.perform(post("/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"messages": [{"role": "user", "content": [{"type": "text", "text": "hi"}]}]}
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.type").value("error"))
                .andExpect(jsonPath("$.error.type").value("backend_error"));
    }

    @Test
    void noBackendAvailable_returns503() throws Exception {
        when(routingService.route(any(), anyString()))
                .thenThrow(new IllegalStateException("No LLM backend available"));

        mvc.perform(post("/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"messages": [{"role": "user", "content": [{"type": "text", "text": "hi"}]}]}
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error.type").value("service_unavailable"));
    }
}
