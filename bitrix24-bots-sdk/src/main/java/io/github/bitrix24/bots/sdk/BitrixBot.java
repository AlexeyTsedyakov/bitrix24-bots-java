package io.github.bitrix24.bots.sdk;

import io.github.bitrix24.bots.api.client.BitrixApiClient;
import io.github.bitrix24.bots.api.client.BitrixApiConfig;
import io.github.bitrix24.bots.sdk.config.BotConfig;
import io.github.bitrix24.bots.sdk.config.PollingConfig;
import io.github.bitrix24.bots.sdk.config.WebhookConfig;
import io.github.bitrix24.bots.sdk.dispatcher.BotEventDispatcher;
import io.github.bitrix24.bots.sdk.listener.BotEventListener;
import io.github.bitrix24.bots.sdk.polling.PollingBotRunner;
import io.github.bitrix24.bots.sdk.webhook.EmbeddedWebhookServer;
import io.github.bitrix24.bots.sdk.webhook.WebhookHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

/**
 * Main facade for creating, configuring, and running Bitrix24 Chat Bots.
 * Seamlessly integrates {@link BitrixApiClient}, {@link BotEventDispatcher},
 * {@link PollingBotRunner}, and {@link EmbeddedWebhookServer}.
 */
@Slf4j
public class BitrixBot implements AutoCloseable {

    private final BitrixApiClient client;
    private final BotConfig botConfig;
    private final PollingConfig pollingConfig;
    private final WebhookConfig webhookConfig;
    private final BotEventDispatcher dispatcher;

    private PollingBotRunner pollingRunner;
    private EmbeddedWebhookServer webhookServer;

    private BitrixBot(Builder builder) {
        this.client = Objects.requireNonNull(builder.client, "BitrixApiClient cannot be null");
        this.botConfig = builder.botConfig != null ? builder.botConfig : BotConfig.builder().build();
        this.pollingConfig = builder.pollingConfig != null ? builder.pollingConfig : PollingConfig.builder().build();
        this.webhookConfig = builder.webhookConfig != null ? builder.webhookConfig : WebhookConfig.builder().build();

        BitrixApiConfig apiConfig = this.client.getConfig();

        // Resolve applicationToken from webhookConfig or apiConfig if absent in botConfig
        if (this.botConfig.getApplicationToken() == null || this.botConfig.getApplicationToken().isBlank()) {
            if (this.webhookConfig.getApplicationToken() != null && !this.webhookConfig.getApplicationToken().isBlank()) {
                this.botConfig.setApplicationToken(this.webhookConfig.getApplicationToken());
            } else if (apiConfig != null && apiConfig.getApplicationToken() != null && !apiConfig.getApplicationToken().isBlank()) {
                this.botConfig.setApplicationToken(apiConfig.getApplicationToken());
            }
        }

        // Propagate botId and botToken from apiConfig to botConfig if not set
        if (apiConfig != null) {
            if (this.botConfig.getBotId() == null && apiConfig.getBotId() != null) {
                this.botConfig.setBotId(apiConfig.getBotId());
            }
            if (this.botConfig.getBotToken() == null && apiConfig.getBotToken() != null) {
                this.botConfig.setBotToken(apiConfig.getBotToken());
            }
        }

        // Propagate botId and botToken from botConfig to pollingConfig if not explicitly overridden
        if (this.pollingConfig.getBotId() == null && this.botConfig.getBotId() != null) {
            this.pollingConfig.setBotId(this.botConfig.getBotId());
        }
        if (this.pollingConfig.getBotToken() == null && this.botConfig.getBotToken() != null) {
            this.pollingConfig.setBotToken(this.botConfig.getBotToken());
        }

        // Initialize dispatcher
        this.dispatcher = new BotEventDispatcher(this.botConfig, builder.listener);
    }

    /**
     * Creates a new {@link Builder} to construct a {@link BitrixBot}.
     *
     * @return bot builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a bot facade using the provided client and default configurations.
     *
     * @param client Bitrix24 REST API client
     * @return configured bot
     */
    public static BitrixBot fromClient(BitrixApiClient client) {
        return builder().client(client).build();
    }

    /**
     * Creates a bot facade using API configuration and bot configuration.
     *
     * @param apiConfig Bitrix24 API connection configuration
     * @param botConfig bot event and security configuration
     * @return configured bot
     */
    public static BitrixBot fromConfig(BitrixApiConfig apiConfig, BotConfig botConfig) {
        return builder()
                .client(BitrixApiClient.create(apiConfig))
                .botConfig(botConfig)
                .build();
    }

    /**
     * Registers a listener for incoming bot events.
     *
     * @param listener event listener
     * @return this bot instance
     */
    public BitrixBot onEvent(BotEventListener listener) {
        this.dispatcher.setListener(listener);
        return this;
    }

    /**
     * Adds an additional listener for incoming bot events.
     *
     * @param listener event listener
     * @return this bot instance
     */
    public BitrixBot addListener(BotEventListener listener) {
        this.dispatcher.addListener(listener);
        return this;
    }

    /**
     * Starts background polling via {@link PollingBotRunner}.
     *
     * @return the running {@link PollingBotRunner}
     */
    public synchronized PollingBotRunner startPolling() {
        if (pollingRunner == null) {
            pollingRunner = new PollingBotRunner(client, dispatcher, pollingConfig);
        }
        return pollingRunner.start();
    }

    /**
     * Stops the background polling runner.
     */
    public synchronized void stopPolling() {
        if (pollingRunner != null) {
            pollingRunner.stop();
            pollingRunner = null;
        }
    }

    /**
     * Starts the embedded webhook HTTP server.
     *
     * @return the running {@link EmbeddedWebhookServer}
     * @throws IOException if server fails to bind
     */
    public synchronized EmbeddedWebhookServer startWebhookServer() throws IOException {
        if (webhookServer == null) {
            webhookServer = new EmbeddedWebhookServer(webhookConfig, dispatcher);
        }
        return webhookServer.start();
    }

    /**
     * Stops the embedded webhook HTTP server.
     */
    public synchronized void stopWebhookServer() {
        if (webhookServer != null) {
            webhookServer.stop();
            webhookServer = null;
        }
    }

    /**
     * Creates a standalone {@link WebhookHandler} for integration into external servlet or reactive containers.
     *
     * @return webhook handler
     */
    public WebhookHandler createWebhookHandler() {
        return new WebhookHandler(dispatcher);
    }

    /**
     * Stops all active runners and servers (both polling and webhook).
     */
    public synchronized void stop() {
        stopPolling();
        stopWebhookServer();
    }

    @Override
    public void close() {
        stop();
        if (client != null) {
            client.close();
        }
    }

    /**
     * Returns the underlying {@link BitrixApiClient}.
     *
     * @return REST API client
     */
    public BitrixApiClient getClient() {
        return client;
    }

    /**
     * Returns the bot event dispatcher.
     *
     * @return event dispatcher
     */
    public BotEventDispatcher getDispatcher() {
        return dispatcher;
    }

    /**
     * Returns the bot configuration.
     *
     * @return bot config
     */
    public BotConfig getBotConfig() {
        return botConfig;
    }

    /**
     * Returns the polling configuration.
     *
     * @return polling config
     */
    public PollingConfig getPollingConfig() {
        return pollingConfig;
    }

    /**
     * Returns the webhook configuration.
     *
     * @return webhook config
     */
    public WebhookConfig getWebhookConfig() {
        return webhookConfig;
    }

    /**
     * Returns the active polling runner, or null if not started.
     *
     * @return active polling runner or null
     */
    public PollingBotRunner getPollingRunner() {
        return pollingRunner;
    }

    /**
     * Returns the active embedded webhook server, or null if not started.
     *
     * @return active webhook server or null
     */
    public EmbeddedWebhookServer getWebhookServer() {
        return webhookServer;
    }

    /**
     * Builder for {@link BitrixBot}.
     */
    public static class Builder {
        private BitrixApiClient client;
        private BotConfig botConfig;
        private PollingConfig pollingConfig;
        private WebhookConfig webhookConfig;
        private BotEventListener listener;

        public Builder client(BitrixApiClient client) {
            this.client = client;
            return this;
        }

        public Builder botConfig(BotConfig botConfig) {
            this.botConfig = botConfig;
            return this;
        }

        public Builder pollingConfig(PollingConfig pollingConfig) {
            this.pollingConfig = pollingConfig;
            return this;
        }

        public Builder webhookConfig(WebhookConfig webhookConfig) {
            this.webhookConfig = webhookConfig;
            return this;
        }

        public Builder listener(BotEventListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder onEvent(BotEventListener listener) {
            this.listener = listener;
            return this;
        }

        public BitrixBot build() {
            return new BitrixBot(this);
        }
    }
}
