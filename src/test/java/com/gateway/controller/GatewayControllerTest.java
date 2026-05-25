package com.gateway.controller;

import com.gateway.model.AssistantResponse;
import com.gateway.service.RoutingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GatewayControllerTest {

    @MockBean RoutingService routingService;

    @Autowired MockMvc mvc;

    private static AssistantResponse response(String text) {
        return new AssistantResponse("test-model", text);
    }

    @Test
    void request_delegatesToRoutingService_returnsAnthropicFormat() throws Exception {
        when(routingService.route(any(), anyString())).thenReturn(response("hello back"));

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
    void emptyMessages_stillCallsRoutingService() throws Exception {
        when(routingService.route(any(), anyString())).thenReturn(response(""));

        mvc.perform(post("/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"messages\": []}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("assistant"));
    }

    @Test
    void missingMessages_stillCallsRoutingService() throws Exception {
        when(routingService.route(any(), anyString())).thenReturn(response(""));

        mvc.perform(post("/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("assistant"));
    }

    @Test
    void authDisabled_anyRequestAccepted() throws Exception {
        when(routingService.route(any(), anyString())).thenReturn(response("ok"));

        mvc.perform(post("/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer some-token")
                        .content("""
                                {"messages": [{"role": "user", "content": [{"type": "text", "text": "hi"}]}]}
                                """))
                .andExpect(status().isOk());
    }
}
