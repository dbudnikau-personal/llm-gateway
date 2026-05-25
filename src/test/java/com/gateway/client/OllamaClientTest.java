package com.gateway.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class OllamaClientTest {

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private OllamaClient client() {
        RestClient restClient = RestClient.builder()
                .requestFactory(new SimpleClientHttpRequestFactory())
                .baseUrl(wm.baseUrl())
                .build();
        return new OllamaClient(restClient);
    }

    @Test
    void ask_parsesResponseField() {
        wm.stubFor(post(urlEqualTo("/api/generate"))
                .willReturn(okJson("{\"model\":\"test\",\"response\":\"pong\",\"done\":true}")));

        assertThat(client().ask("test-model", "ping")).isEqualTo("pong");
    }

    @Test
    void ask_nullBody_returnsNull() {
        wm.stubFor(post(urlEqualTo("/api/generate"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("null")));

        assertThat(client().ask("test-model", "ping")).isNull();
    }
}
