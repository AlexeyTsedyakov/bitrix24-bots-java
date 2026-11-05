package io.github.bitrix24.bots.sdk.example;

import io.github.bitrix24.bots.api.client.BitrixApiClient;
import io.github.bitrix24.bots.api.client.BitrixApiConfig;
import io.github.bitrix24.bots.api.model.chat.SendMessageFields;
import io.github.bitrix24.bots.api.model.chat.SendMessageRequest;
import io.github.bitrix24.bots.api.model.chat.keyboard.Keyboard;
import io.github.bitrix24.bots.api.model.chat.keyboard.KeyboardButton;
import io.github.bitrix24.bots.api.model.command.CommandAnswerFields;
import io.github.bitrix24.bots.api.model.command.CommandAnswerRequest;
import io.github.bitrix24.bots.api.model.event.Event;
import io.github.bitrix24.bots.sdk.BitrixBot;
import io.github.bitrix24.bots.sdk.config.BotConfig;
import io.github.bitrix24.bots.sdk.config.WebhookConfig;
import io.github.bitrix24.bots.sdk.webhook.EmbeddedWebhookServer;
import lombok.extern.slf4j.Slf4j;

/**
 * Example demonstrating a Webhook Bot running an embedded HTTP receiver.
 * Handles incoming messages ({@code ONIMBOTV2MESSAGEADD}), slash commands ({@code ONIMBOTV2COMMANDADD}),
 * and greetings upon joining chat ({@code ONIMBOTV2JOINCHAT}).
 */
@Slf4j
public class WebhookBotExample {

    public static void main(String[] args) throws Exception {
        // Read connection and security parameters from environment
        String webhookUrl = System.getenv().getOrDefault("BITRIX24_WEBHOOK_URL", "https://your-portal.bitrix24.ru/rest/1/webhook-key/");
        Long botId = Long.parseLong(System.getenv().getOrDefault("BITRIX24_BOT_ID", "123"));
        String botToken = System.getenv().getOrDefault("BITRIX24_BOT_TOKEN", "my_bot_token_secret");
        String appToken = System.getenv().getOrDefault("BITRIX24_APP_TOKEN", "831c76b092f9f135d9b6b36c3a720757");
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

        // 1. Configure REST client and Bot settings
        BitrixApiConfig apiConfig = BitrixApiConfig.fromWebhook(webhookUrl, botId, botToken);
        BitrixApiClient client = BitrixApiClient.create(apiConfig);

        BotConfig botConfig = BotConfig.builder()
                .botId(botId)
                .botToken(botToken)
                .applicationToken(appToken) // validates incoming Bitrix24 webhook token
                .build();

        WebhookConfig webhookConfig = WebhookConfig.builder()
                .port(port)
                .path("/bitrix24/webhook")
                .build();

        // 2. Assemble bot facade with business logic
        BitrixBot bot = BitrixBot.builder()
                .client(client)
                .botConfig(botConfig)
                .webhookConfig(webhookConfig)
                .onEvent(event -> handleEvent(client, event))
                .build();

        // 3. Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down webhook server...");
            bot.close();
        }));

        // 4. Start embedded HTTP server
        EmbeddedWebhookServer server = bot.startWebhookServer();
        log.info("Webhook server listening at: {}", server.getUrl());
        log.info("Configure your bot in Bitrix24 with webhook URL pointing to this endpoint.");
    }

    private static void handleEvent(BitrixApiClient client, Event event) {
        if (event.isMessage()) {
            handleMessage(client, event);
        } else if (event.isCommand()) {
            handleCommand(client, event);
        } else if (event.isJoinChat()) {
            handleJoinChat(client, event);
        }
    }

    private static void handleMessage(BitrixApiClient client, Event event) {
        String dialogId = event.getDialogId();
        String messageText = event.getMessageText();

        log.info("Received message in dialog {}: {}", dialogId, messageText);

        SendMessageRequest echo = SendMessageRequest.builder()
                .dialogId(dialogId)
                .fields(SendMessageFields.of("Эхо: " + (messageText != null ? messageText : "")))
                .build();

        try {
            client.message().send(echo);
            log.info("Sent echo message to dialog {}", dialogId);
        } catch (Exception e) {
            log.error("Failed to send echo message: {}", e.getMessage(), e);
        }
    }

    private static void handleCommand(BitrixApiClient client, Event event) {
        String dialogId = event.getDialogId();
        String commandName = event.getCommandName();
        String commandParams = event.getCommandParams();
        Long commandId = event.getData() != null && event.getData().getCommand() != null
                ? event.getData().getCommand().getId() : null;
        Long messageId = event.getData() != null && event.getData().getMessage() != null
                ? event.getData().getMessage().getId() : null;

        log.info("Executing slash command: {} with params: {}", commandName, commandParams);

        String replyText = resolveCommandReply(commandName);

        Keyboard keyboard = Keyboard.of(
                KeyboardButton.command("Помощь", "/help"),
                KeyboardButton.command("Статус", "/status")
        );

        CommandAnswerRequest answer = CommandAnswerRequest.builder()
                .commandId(commandId)
                .messageId(messageId)
                .dialogId(dialogId)
                .fields(CommandAnswerFields.builder()
                        .message(replyText)
                        .keyboard(keyboard)
                        .build())
                .build();

        try {
            client.command().answer(answer);
            log.info("Published answer for command {}", commandName);
        } catch (Exception e) {
            log.error("Failed to answer command: {}", e.getMessage(), e);
        }
    }

    private static String resolveCommandReply(String commandName) {
        if ("/help".equalsIgnoreCase(commandName)) {
            return "Список доступных команд:\n/help - помощь\n/status - проверить статус бота";
        }
        if ("/status".equalsIgnoreCase(commandName)) {
            return "✅ Бот работает в штатном режиме (режим Webhook)";
        }
        return "Неизвестная команда: " + commandName;
    }

    private static void handleJoinChat(BitrixApiClient client, Event event) {
        String dialogId = event.getDialogId();
        log.info("Bot was invited to chat {}", dialogId);

        SendMessageRequest welcome = SendMessageRequest.builder()
                .dialogId(dialogId)
                .fields(SendMessageFields.of("Всем привет! Я чат-бот Битрикс24. Напишите /help, чтобы увидеть мои команды."))
                .build();

        try {
            client.message().send(welcome);
        } catch (Exception e) {
            log.error("Failed to send welcome message: {}", e.getMessage(), e);
        }
    }
}
