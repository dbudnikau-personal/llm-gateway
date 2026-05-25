package com.gateway.controller;

import com.gateway.client.AnthropicClient;
import com.gateway.client.OllamaClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "gateway.auth.token=secret-token")
class GatewayControllerAuthTest {

    @MockBean OllamaClient ollamaClient;
    @MockBean AnthropicClient anthropicClient;

    @Autowired MockMvc mvc;

    @Test
    void correctToken_allowsRequest() throws Exception {
        when(anthropicClient.isEnabled()).thenReturn(false);
        when(ollamaClient.ask(any(), any())).thenReturn("ok");

        mvc.perform(post("/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer secret-token")
                        .content("""
                                {"messages": [{"role": "user", "content": [{"type": "text", "text": "hi"}]}]}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void wrongToken_returns401() throws Exception {
        mvc.perform(post("/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer wrong")
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value("error"))
                .andExpect(jsonPath("$.error.type").value("authentication_error"));
    }

    @Test
    void missingToken_returns401() throws Exception {
        mvc.perform(post("/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
