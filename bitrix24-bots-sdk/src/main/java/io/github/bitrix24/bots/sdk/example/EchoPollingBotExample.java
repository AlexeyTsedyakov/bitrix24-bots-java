package io.github.bitrix24.bots.sdk.example;

import io.github.bitrix24.bots.api.client.BitrixApiClient;
import io.github.bitrix24.bots.api.client.BitrixApiConfig;
import io.github.bitrix24.bots.api.model.chat.SendMessageFields;
import io.github.bitrix24.bots.api.model.chat.SendMessageRequest;
import io.github.bitrix24.bots.api.model.chat.keyboard.Keyboard;
import io.github.bitrix24.bots.api.model.chat.keyboard.KeyboardButton;
import io.github.bitrix24.bots.api.model.event.Event;
import io.github.bitrix24.bots.sdk.BitrixBot;
import io.github.bitrix24.bots.sdk.config.BotConfig;
import io.github.bitrix24.bots.sdk.config.PollingConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * Example demonstrating an Echo Bot running in polling (FETCH) mode via {@code imbot.v2.Event.get}.
 * Requires no public IP or SSL certificate — ideal for local development and testing.
 */
@Slf4j
public class EchoPollingBotExample {

    public static void main(String[] args) {
        // Read connection parameters from environment or use defaults
        String webhookUrl = System.getenv().getOrDefault("BITRIX24_WEBHOOK_URL", "https://your-portal.bitrix24.ru/rest/1/webhook-key/");
        Long botId = Long.parseLong(System.getenv().getOrDefault("BITRIX24_BOT_ID", "123"));
        String botToken = System.getenv().getOrDefault("BITRIX24_BOT_TOKEN", "my_bot_token_secret");

        // 1. Configure REST API client
        BitrixApiConfig apiConfig = BitrixApiConfig.fromWebhook(webhookUrl, botId, botToken);
        BitrixApiClient client = BitrixApiClient.create(apiConfig);

        // 2. Configure bot and polling runner
        BotConfig botConfig = BotConfig.builder()
                .botId(botId)
                .botToken(botToken)
                .build();

        PollingConfig pollingConfig = PollingConfig.builder()
                .intervalMs(1000L) // poll every 1 second when queue is empty
                .limit(100)
                .build();

        // 3. Assemble bot facade
        BitrixBot bot = BitrixBot.builder()
                .client(client)
                .botConfig(botConfig)
                .pollingConfig(pollingConfig)
                .onEvent(event -> handleEvent(client, event))
                .build();

        // 4. Register shutdown hook for graceful exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down echo bot...");
            bot.close();
        }));

        // 5. Start polling loop
        log.info("Starting Bitrix24 echo bot in polling mode...");
        bot.startPolling();

        log.info("Echo bot is running. Send a message to the bot in Bitrix24 chat! Press Ctrl+C to stop.");
    }

    private static void handleEvent(BitrixApiClient client, Event event) {
        if (!event.isMessage()) {
            return;
        }

        String dialogId = event.getDialogId();
        String text = event.getMessageText();
        String userName = event.getUserName() != null ? event.getUserName() : "пользователь";

        log.info("Received message from {} in chat {}: {}", userName, dialogId, text);

        // Create interactive keyboard
        Keyboard keyboard = Keyboard.of(
                KeyboardButton.command("💡 Помощь", "/help"),
                KeyboardButton.link("📖 Документация", "https://apidocs.bitrix24.ru/api-reference/chat-bots/chat-bots-v2/index.html")
        );

        // Build reply message
        SendMessageRequest reply = SendMessageRequest.builder()
                .dialogId(dialogId)
                .fields(SendMessageFields.builder()
                        .message("Привет, " + userName + "! Ты написал: " + text)
                        .keyboard(keyboard)
                        .build())
                .build();

        try {
            client.message().send(reply);
            log.info("Sent reply to dialog {}", dialogId);
        } catch (Exception e) {
            log.error("Failed to send message reply: {}", e.getMessage(), e);
        }
    }
}
