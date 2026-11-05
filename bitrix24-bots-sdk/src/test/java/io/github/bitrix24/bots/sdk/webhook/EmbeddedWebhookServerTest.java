package io.github.bitrix24.bots.sdk.webhook;

import io.github.bitrix24.bots.api.model.event.Event;
import io.github.bitrix24.bots.sdk.config.BotConfig;
import io.github.bitrix24.bots.sdk.config.WebhookConfig;
import io.github.bitrix24.bots.sdk.dispatcher.BotEventDispatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddedWebhookServerTest {

    private EmbeddedWebhookServer server;
    private List<Event> receivedEvents;
    private HttpClient httpClient;

    @BeforeEach
    void setUp() throws Exception {
        receivedEvents = new CopyOnWriteArrayList<>();
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();

        BotConfig botConfig = BotConfig.builder()
                .applicationToken("test_secret_app_token")
                .build();

        BotEventDispatcher dispatcher = new BotEventDispatcher(botConfig, receivedEvents::add);

        WebhookConfig webhookConfig = WebhookConfig.builder()
                .host("127.0.0.1")
                .port(0) // allocate ephemeral free port
                .path("/webhook/b24")
                .build();

        server = new EmbeddedWebhookServer(webhookConfig, dispatcher);
        server.start();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    @DisplayName("Embedded server starts on free port and processes incoming JSON webhook")
    void testReceiveJsonWebhook() throws Exception {
        assertThat(server.isRunning()).isTrue();
        int port = server.getPort();
        assertThat(port).isGreaterThan(0);

        String jsonPayload = """
                {
                    "event": "ONIMBOTV2MESSAGEADD",
                    "data": {
                        "message": {
                            "id": 999,
                            "text": "Hello from embedded server test"
                        },
                        "chat": {
                            "id": "10",
                            "dialogId": "chat10"
                        }
                    },
                    "ts": 1736937000,
                    "auth": {
                        "domain": "test.bitrix24.ru",
                        "application_token": "test_secret_app_token"
                    }
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + "/webhook/b24"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"result\":\"OK\"");

        assertThat(receivedEvents).hasSize(1);
        Event event = receivedEvents.get(0);
        assertThat(event.isMessage()).isTrue();
        assertThat(event.getMessageText()).isEqualTo("Hello from embedded server test");
        assertThat(event.getChatId()).isEqualTo("10");
        assertThat(event.getDialogId()).isEqualTo("chat10");
    }

    @Test
    @DisplayName("Embedded server processes incoming application/x-www-form-urlencoded webhook")
    void testReceiveFormWebhook() throws Exception {
        int port = server.getPort();

        String formPayload = "event=ONIMBOTV2COMMANDADD"
                + "&auth%5Bapplication_token%5D=test_secret_app_token"
                + "&data%5Bcommand%5D%5Bid%5D=5"
                + "&data%5Bcommand%5D%5Bcommand%5D=%2Fstart"
                + "&data%5Bcommand%5D%5Bparams%5D=welcome"
                + "&data%5Bchat%5D%5Bid%5D=55";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + "/webhook/b24"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(receivedEvents).hasSize(1);
        Event event = receivedEvents.get(0);
        assertThat(event.isCommand()).isTrue();
        assertThat(event.getCommandName()).isEqualTo("/start");
        assertThat(event.getCommandParams()).isEqualTo("welcome");
        assertThat(event.getChatId()).isEqualTo("55");
    }

    @Test
    @DisplayName("Rejects incoming webhook with 401 when application token does not match")
    void testRejectInvalidToken() throws Exception {
        int port = server.getPort();

        String jsonPayload = """
                {
                    "event": "ONIMBOTV2MESSAGEADD",
                    "auth": {
                        "application_token": "fraudulent_token"
                    }
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + "/webhook/b24"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).contains("INVALID_APPLICATION_TOKEN");
        assertThat(receivedEvents).isEmpty();
    }

    @Test
    @DisplayName("Rejects non-POST requests with 405 Method Not Allowed")
    void testRejectGetMethod() throws Exception {
        int port = server.getPort();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + "/webhook/b24"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(405);
        assertThat(response.body()).contains("METHOD_NOT_ALLOWED");
    }
}
