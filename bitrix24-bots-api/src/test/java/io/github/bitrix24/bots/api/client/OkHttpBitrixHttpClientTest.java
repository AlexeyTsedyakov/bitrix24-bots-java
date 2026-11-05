package io.github.bitrix24.bots.api.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OkHttpBitrixHttpClientTest {

    private MockWebServer server;
    private OkHttpBitrixHttpClient httpClient;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        httpClient = OkHttpBitrixHttpClient.builder()
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(5))
                .writeTimeout(Duration.ofSeconds(5))
                .enableLogging(true)
                .build();
    }

    @AfterEach
    void tearDown() throws IOException {
        httpClient.close();
        server.shutdown();
    }

    @Test
    @DisplayName("Should execute POST request with JSON body and custom headers")
    void testExecutePostSuccess() throws InterruptedException, IOException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"result\": true}"));

        String url = server.url("/rest/1/secret_token/test.method").toString();
        BitrixHttpResponse response = httpClient.executePost(url, "{\"foo\": \"bar\"}", Map.of("X-Custom", "Value123"));

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo("{\"result\": true}");

        RecordedRequest recordedRequest = server.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getHeader("Accept")).isEqualTo("application/json");
        assertThat(recordedRequest.getHeader("X-Custom")).isEqualTo("Value123");
        assertThat(recordedRequest.getBody().readUtf8()).isEqualTo("{\"foo\": \"bar\"}");
    }

    @Test
    @DisplayName("Should return response body and status code on HTTP error (400 Bad Request)")
    void testExecutePostHttpError() throws IOException {
        server.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"error\": \"EMPTY_MESSAGE\", \"error_description\": \"Message is empty\"}"));

        String url = server.url("/rest/1/secret/test.method").toString();
        BitrixHttpResponse response = httpClient.executePost(url, "{}");

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.isSuccessful()).isFalse();
        assertThat(response.getBody()).contains("EMPTY_MESSAGE");
    }

    @Test
    @DisplayName("Should mask webhook secrets in URLs")
    void testMaskUrl() {
        String url1 = "https://portal.bitrix24.com/rest/42/ab12cd34ef56/imbot.v2.Bot.register";
        assertThat(OkHttpBitrixHttpClient.maskUrl(url1))
                .isEqualTo("https://portal.bitrix24.com/rest/42/***/imbot.v2.Bot.register");

        String url2 = "https://portal.bitrix24.com/rest/100/webhookKey/?auth=abc";
        assertThat(OkHttpBitrixHttpClient.maskUrl(url2))
                .isEqualTo("https://portal.bitrix24.com/rest/100/***/?auth=abc");

        assertThat(OkHttpBitrixHttpClient.maskUrl(null)).isNull();
        assertThat(OkHttpBitrixHttpClient.maskUrl("https://portal.bitrix24.com/rest/imbot.v2.Bot.get"))
                .isEqualTo("https://portal.bitrix24.com/rest/imbot.v2.Bot.get");
    }
}
