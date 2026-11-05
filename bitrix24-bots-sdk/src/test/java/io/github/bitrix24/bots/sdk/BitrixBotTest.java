package io.github.bitrix24.bots.sdk;

import io.github.bitrix24.bots.api.client.*;
import io.github.bitrix24.bots.api.model.common.BitrixResponse;
import io.github.bitrix24.bots.api.model.common.batch.BatchRequest;
import io.github.bitrix24.bots.api.model.common.batch.BatchResult;
import io.github.bitrix24.bots.api.model.event.EventGetResult;
import io.github.bitrix24.bots.sdk.config.BotConfig;
import io.github.bitrix24.bots.sdk.config.PollingConfig;
import io.github.bitrix24.bots.sdk.config.WebhookConfig;
import io.github.bitrix24.bots.sdk.polling.PollingBotRunner;
import io.github.bitrix24.bots.sdk.webhook.EmbeddedWebhookServer;
import io.github.bitrix24.bots.sdk.webhook.WebhookHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JavaType;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class BitrixBotTest {

    @Test
    @DisplayName("Builds BitrixBot with fluent builder and initializes components")
    void testBuilderInitialization() {
        AtomicBoolean closed = new AtomicBoolean(false);
        BitrixApiClient client = new DummyClient(closed);

        BotConfig botConfig = BotConfig.builder()
                .botId(101L)
                .botToken("token_101")
                .applicationToken("app_token_101")
                .build();

        PollingConfig pollingConfig = PollingConfig.builder()
                .limit(50)
                .intervalMs(500)
                .build();

        WebhookConfig webhookConfig = WebhookConfig.builder()
                .port(8081)
                .path("/custom/path")
                .build();

        BitrixBot bot = BitrixBot.builder()
                .client(client)
                .botConfig(botConfig)
                .pollingConfig(pollingConfig)
                .webhookConfig(webhookConfig)
                .onEvent(event -> {})
                .build();

        assertThat(bot.getClient()).isSameAs(client);
        assertThat(bot.getBotConfig()).isSameAs(botConfig);
        assertThat(bot.getPollingConfig()).isSameAs(pollingConfig);
        assertThat(bot.getWebhookConfig()).isSameAs(webhookConfig);
        assertThat(bot.getDispatcher()).isNotNull();

        bot.close();
        assertThat(closed.get()).isTrue();
    }

    @Test
    @DisplayName("Starts and stops polling runner through facade")
    void testPollingLifecycle() {
        DummyClient client = new DummyClient(new AtomicBoolean());

        BitrixBot bot = BitrixBot.builder()
                .client(client)
                .pollingConfig(PollingConfig.builder().intervalMs(200).build())
                .build();

        assertThat(bot.getPollingRunner()).isNull();

        PollingBotRunner runner = bot.startPolling();
        assertThat(runner).isNotNull();
        assertThat(runner.isRunning()).isTrue();
        assertThat(bot.getPollingRunner()).isSameAs(runner);

        bot.stopPolling();
        assertThat(runner.isRunning()).isFalse();
        assertThat(bot.getPollingRunner()).isNull();
    }

    @Test
    @DisplayName("Starts and stops embedded webhook server through facade")
    void testWebhookServerLifecycle() throws IOException {
        DummyClient client = new DummyClient(new AtomicBoolean());

        BitrixBot bot = BitrixBot.builder()
                .client(client)
                .webhookConfig(WebhookConfig.builder().host("127.0.0.1").port(0).build())
                .build();

        assertThat(bot.getWebhookServer()).isNull();

        EmbeddedWebhookServer server = bot.startWebhookServer();
        assertThat(server).isNotNull();
        assertThat(server.isRunning()).isTrue();
        assertThat(server.getPort()).isGreaterThan(0);
        assertThat(bot.getWebhookServer()).isSameAs(server);

        bot.stopWebhookServer();
        assertThat(server.isRunning()).isFalse();
        assertThat(bot.getWebhookServer()).isNull();
    }

    @Test
    @DisplayName("Creates standalone WebhookHandler from facade")
    void testCreateWebhookHandler() {
        DummyClient client = new DummyClient(new AtomicBoolean());

        BitrixBot bot = BitrixBot.builder()
                .client(client)
                .build();

        WebhookHandler handler = bot.createWebhookHandler();
        assertThat(handler).isNotNull();

        WebhookHandler.WebhookResult result = handler.handle("GET", null, "");
        assertThat(result.statusCode()).isEqualTo(405);
    }

    // --- Dummy Client for testing facade ---

    private record DummyClient(AtomicBoolean closed) implements BitrixApiClient {

        @Override
        public BitrixApiConfig getConfig() {
            return null;
        }

        @Override
        public BitrixHttpClient getHttpClient() {
            return null;
        }

        @Override
        public BotApi bot() {
            return null;
        }

        @Override
        public MessageApi message() {
            return null;
        }

        @Override
        public CommandApi command() {
            return null;
        }

        @Override
        public EventApi event() {
            return request -> BitrixResponse.ok(EventGetResult.builder().events(Collections.emptyList()).build());
        }

        @Override
        public FileApi file() {
            return null;
        }

        @Override
        public <T> BitrixResponse<T> callMethod(String m, Object p, Class<T> r) {
            return null;
        }

        @Override
        public <T> BitrixResponse<T> callMethod(String m, Object p, TypeReference<T> r) {
            return null;
        }

        @Override
        public <T> BitrixResponse<T> callMethod(String m, Object p, JavaType r) {
            return null;
        }

        @Override
        public BitrixResponse<BatchResult> batch(BatchRequest request) {
            return null;
        }

        @Override
            public void close() {
                closed.set(true);
            }
        }
}
