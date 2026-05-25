package com.gateway.controller;

import com.gateway.client.AnthropicClient;
import com.gateway.client.OllamaClient;
import com.gateway.dto.AnthropicResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GatewayControllerTest {

    @MockBean OllamaClient ollamaClient;
    @MockBean AnthropicClient anthropicClient;

    @Autowired MockMvc mvc;

    @Test
    void simplePrompt_routedToOllama_returnsAnthropicFormat() throws Exception {
        when(anthropicClient.isEnabled()).thenReturn(false);
        when(ollamaClient.ask(any(), any())).thenReturn("hello back");

        mvc.perform(post("/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "model": "coder",
                                  "messages": [
                                    {"role": "user", "content": [{"type": "text", "text": "hi"}]}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("message"))
                .andExpect(jsonPath("$.role").value("assistant"))
                .andExpect(jsonPath("$.content[0].type").value("text"))
                .andExpect(jsonPath("$.content[0].text").value("hello back"))
                .andExpect(jsonPath("$.stop_reason").value("end_turn"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void complexPrompt_routedToAnthropic() throws Exception {
        when(anthropicClient.isEnabled()).thenReturn(true);
        AnthropicResponse mockResponse = new AnthropicResponse(
                "msg_abc", "message", "assistant", "claude-sonnet-4-6",
                List.of(new AnthropicResponse.ContentBlock("text", "deep answer")),
                "end_turn", null,
                new AnthropicResponse.Usage(10, 20)
        );
        when(anthropicClient.ask(any())).thenReturn(mockResponse);

        mvc.perform(post("/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "messages": [
                                    {"role": "user", "content": [{"type": "text", "text": "explain why the sky is blue and analyze the physics"}]}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("assistant"))
                .andExpect(jsonPath("$.content[0].text").value("deep answer"));
    }

    @Test
    void emptyMessages_returns200WithEmptyReply() throws Exception {
        when(anthropicClient.isEnabled()).thenReturn(false);

        mvc.perform(post("/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"messages\": []}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("assistant"));
    }

    @Test
    void missingMessages_returns200WithEmptyReply() throws Exception {
        when(anthropicClient.isEnabled()).thenReturn(false);

        mvc.perform(post("/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("assistant"));
    }

    @Test
    void authDisabled_anyRequestAccepted() throws Exception {
        when(anthropicClient.isEnabled()).thenReturn(false);
        when(ollamaClient.ask(any(), any())).thenReturn("ok");

        mvc.perform(post("/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer some-token")
                        .content("""
                                {"messages": [{"role": "user", "content": [{"type": "text", "text": "hi"}]}]}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void ollamaReturnsNull_responseIsEmpty() throws Exception {
        when(anthropicClient.isEnabled()).thenReturn(false);
        when(ollamaClient.ask(any(), any())).thenReturn(null);

        mvc.perform(post("/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"messages": [{"role": "user", "content": [{"type": "text", "text": "hi"}]}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].text").value(""));
    }
}
