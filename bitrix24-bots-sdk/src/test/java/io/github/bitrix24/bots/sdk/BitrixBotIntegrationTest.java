package io.github.bitrix24.bots.sdk;

import io.github.bitrix24.bots.api.client.*;
import io.github.bitrix24.bots.api.json.BitrixJsonMapper;
import io.github.bitrix24.bots.api.model.bot.*;
import io.github.bitrix24.bots.api.model.chat.SendMessageFields;
import io.github.bitrix24.bots.api.model.chat.SendMessageRequest;
import io.github.bitrix24.bots.api.model.chat.SendMessageResult;
import io.github.bitrix24.bots.api.model.chat.keyboard.Keyboard;
import io.github.bitrix24.bots.api.model.chat.keyboard.KeyboardButton;
import io.github.bitrix24.bots.api.model.command.CommandAnswerRequest;
import io.github.bitrix24.bots.api.model.command.CommandRegisterFields;
import io.github.bitrix24.bots.api.model.command.CommandRegisterRequest;
import io.github.bitrix24.bots.api.model.command.CommandRegisterResult;
import io.github.bitrix24.bots.api.model.common.BitrixResponse;
import io.github.bitrix24.bots.api.model.common.batch.BatchRequest;
import io.github.bitrix24.bots.api.model.common.batch.BatchResult;
import io.github.bitrix24.bots.api.model.event.Event;
import io.github.bitrix24.bots.api.model.event.EventGetResult;
import io.github.bitrix24.bots.sdk.config.BotConfig;
import io.github.bitrix24.bots.sdk.config.PollingConfig;
import io.github.bitrix24.bots.sdk.polling.PollingBotRunner;
import io.github.bitrix24.bots.sdk.webhook.WebhookHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JavaType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class BitrixBotIntegrationTest {

    @Test
    @DisplayName("End-to-end scenario: register bot, register command, send message with keyboard, handle incoming webhook event and answer command")
    void testEndToEndWebhookFlow() {
        MockBitrixApiClient mockClient = new MockBitrixApiClient();

        // 1. Register bot via client
        BotRegisterRequest registerReq = BotRegisterRequest.builder()
                .fields(BotRegisterFields.builder()
                        .code("integration_test_bot")
                        .type("bot")
                        .eventMode("webhook")
                        .botToken("secret_bot_token_123")
                        .build())
                .build();
        BitrixResponse<BotRegisterResult> registerRes = mockClient.bot().register(registerReq);
        Long botId = registerRes.getResult().getBot().getId();
        assertThat(botId).isEqualTo(777L);

        // 2. Register slash command
        CommandRegisterRequest commandReq = CommandRegisterRequest.builder()
                .botId(botId)
                .botToken("secret_bot_token_123")
                .fields(CommandRegisterFields.builder()
                        .command("help")
                        .common(true)
                        .build())
                .build();
        BitrixResponse<CommandRegisterResult> commandRes = mockClient.command().register(commandReq);
        Long commandId = commandRes.getResult().getCommand().getId();
        assertThat(commandId).isEqualTo(888L);

        // 3. Assemble BitrixBot facade
        BotConfig botConfig = BotConfig.builder()
                .botId(botId)
                .botToken("secret_bot_token_123")
                .applicationToken("app_token_valid")
                .build();

        List<Event> processedEvents = new CopyOnWriteArrayList<>();
        List<String> answersPublished = new CopyOnWriteArrayList<>();

        BitrixBot bot = BitrixBot.builder()
                .client(mockClient)
                .botConfig(botConfig)
                .onEvent(event -> {
                    processedEvents.add(event);
                    if (event.isCommand()) {
                        // Answer the command
                        mockClient.command().answer(CommandAnswerRequest.of(
                                commandId,
                                event.getData().getMessage().getId(),
                                event.getDialogId(),
                                "Executed command: " + event.getCommandName()
                        ));
                        answersPublished.add(event.getCommandName());
                    }
                })
                .build();

        // 4. Send message with interactive keyboard from bot
        Keyboard keyboard = Keyboard.of(
                KeyboardButton.command("💡 Help", "/help"),
                KeyboardButton.link("🌐 Portal", "https://portal.bitrix24.com")
        );
        SendMessageRequest sendReq = SendMessageRequest.builder()
                .botId(botId)
                .dialogId("chat5")
                .fields(SendMessageFields.builder()
                        .message("Bot is online!")
                        .keyboard(keyboard)
                        .build())
                .build();
        BitrixResponse<SendMessageResult> sendRes = mockClient.message().send(sendReq);
        assertThat(sendRes.getResult().getId()).isEqualTo(1001L);

        // 5. Simulate incoming webhook request (PHP http_build_query format)
        WebhookHandler webhookHandler = bot.createWebhookHandler();
        String formPayload = "event=ONIMBOTV2COMMANDADD"
                + "&auth%5Bapplication_token%5D=app_token_valid"
                + "&data%5Bcommand%5D%5Bid%5D=888"
                + "&data%5Bcommand%5D%5Bcommand%5D=%2Fhelp"
                + "&data%5Bchat%5D%5Bid%5D=5"
                + "&data%5Bchat%5D%5BdialogId%5D=chat5"
                + "&data%5Bmessage%5D%5Bid%5D=2002"
                + "&data%5Bmessage%5D%5Btext%5D=%2Fhelp"
                + "&data%5Buser%5D%5Bid%5D=42"
                + "&data%5Buser%5D%5Bname%5D=Alexander";

        WebhookHandler.WebhookResult webhookResult = webhookHandler.handle(
                "POST",
                "application/x-www-form-urlencoded",
                formPayload
        );

        assertThat(webhookResult.statusCode()).isEqualTo(200);
        assertThat(processedEvents).hasSize(1);

        Event event = processedEvents.get(0);
        assertThat(event.isCommand()).isTrue();
        assertThat(event.getCommandName()).isEqualTo("/help");
        assertThat(event.getUserName()).isEqualTo("Alexander");
        assertThat(event.getChatId()).isEqualTo("5");
        assertThat(event.getDialogId()).isEqualTo("chat5");

        // Verify command answer was triggered
        assertThat(answersPublished).containsExactly("/help");
        assertThat(mockClient.recordedAnswers).hasSize(1);
        assertThat(mockClient.recordedAnswers.get(0).getFields().getMessage()).isEqualTo("Executed command: /help");

        bot.close();
    }

    @Test
    @DisplayName("End-to-end scenario: polling mode cycle with message echoing")
    void testEndToEndPollingFlow() {
        MockBitrixApiClient mockClient = new MockBitrixApiClient();

        List<Event> polledEvents = new CopyOnWriteArrayList<>();
        List<String> repliesSent = new CopyOnWriteArrayList<>();

        BitrixBot bot = BitrixBot.builder()
                .client(mockClient)
                .botConfig(BotConfig.builder().botId(500L).botToken("polling_tok").build())
                .pollingConfig(PollingConfig.builder().initialOffset(10L).build())
                .onEvent(event -> {
                    polledEvents.add(event);
                    if (event.isMessage()) {
                        mockClient.message().send(SendMessageRequest.of(
                                event.getDialogId(),
                                "Echo: " + event.getMessageText()
                        ));
                        repliesSent.add(event.getMessageText());
                    }
                })
                .build();

        // Enqueue 2 events in mock polling queue
        Event msg1 = BitrixJsonMapper.fromJson("""
                {
                    "eventId": 11,
                    "type": "ONIMBOTV2MESSAGEADD",
                    "date": "2025-01-15T10:30:00Z",
                    "data": {"message":{"id":501,"text":"First message"},"chat":{"id":"9","dialogId":"chat9"}}
                }
                """, Event.class);

        Event msg2 = BitrixJsonMapper.fromJson("""
                {
                    "eventId": 12,
                    "type": "ONIMBOTV2MESSAGEADD",
                    "date": "2025-01-15T10:30:01Z",
                    "data": {"message":{"id":502,"text":"Second message"},"chat":{"id":"9","dialogId":"chat9"}}
                }
                """, Event.class);

        mockClient.queuedEvents.add(EventGetResult.builder()
                .events(List.of(msg1, msg2))
                .nextOffset(13L)
                .hasMore(false)
                .build());

        PollingBotRunner runner = new PollingBotRunner(mockClient, bot.getDispatcher(), bot.getPollingConfig());
        assertThat(runner.getCurrentOffset()).isEqualTo(10L);

        // Execute one polling step
        boolean hasMore = runner.pollOnce();

        assertThat(hasMore).isFalse();
        assertThat(polledEvents).hasSize(2);
        assertThat(repliesSent).containsExactly("First message", "Second message");
        assertThat(runner.getCurrentOffset()).isEqualTo(13L);

        assertThat(mockClient.recordedSentMessages).hasSize(2);
        assertThat(mockClient.recordedSentMessages.get(0).getFields().getMessage()).isEqualTo("Echo: First message");
        assertThat(mockClient.recordedSentMessages.get(1).getFields().getMessage()).isEqualTo("Echo: Second message");

        runner.close();
        bot.close();
    }

    // --- Mock Client ---

    private static class MockBitrixApiClient implements BitrixApiClient {
        final List<CommandAnswerRequest> recordedAnswers = new ArrayList<>();
        final List<SendMessageRequest> recordedSentMessages = new ArrayList<>();
        final List<EventGetResult> queuedEvents = new ArrayList<>();
        final AtomicLong messageIdGen = new AtomicLong(1000L);

        @Override public BitrixApiConfig getConfig() { return null; }
        @Override public BitrixHttpClient getHttpClient() { return null; }

        @Override
        public BotApi bot() {
            return new BotApi() {
                @Override public BitrixResponse<BotRegisterResult> register(BotRegisterRequest r) {
                    return BitrixResponse.ok(BotRegisterResult.builder()
                            .bot(io.github.bitrix24.bots.api.model.bot.Bot.builder().id(777L).build())
                            .build());
                }
                @Override public BitrixResponse<io.github.bitrix24.bots.api.model.bot.BotUpdateResult> update(io.github.bitrix24.bots.api.model.bot.BotUpdateRequest r) { return null; }
                @Override public BitrixResponse<io.github.bitrix24.bots.api.model.bot.BotGetResult> get(io.github.bitrix24.bots.api.model.bot.BotGetRequest r) { return null; }
                @Override public BitrixResponse<io.github.bitrix24.bots.api.model.bot.BotListResult> list(io.github.bitrix24.bots.api.model.bot.BotListRequest r) { return null; }
                @Override public BitrixResponse<BotUnregisterResult> unregister(BotUnregisterRequest r) { return null; }
            };
        }

        @Override
        public MessageApi message() {
            return new MessageApi() {
                @Override
                public BitrixResponse<SendMessageResult> send(SendMessageRequest request) {
                    recordedSentMessages.add(request);
                    return BitrixResponse.ok(SendMessageResult.builder().id(messageIdGen.incrementAndGet()).build());
                }
                @Override public BitrixResponse<Boolean> update(io.github.bitrix24.bots.api.model.chat.UpdateMessageRequest r) { return BitrixResponse.ok(true); }
                @Override public BitrixResponse<Boolean> delete(io.github.bitrix24.bots.api.model.chat.DeleteMessageRequest r) { return BitrixResponse.ok(true); }
                @Override public BitrixResponse<io.github.bitrix24.bots.api.model.chat.ReadMessageResult> read(io.github.bitrix24.bots.api.model.chat.ReadMessageRequest r) { return null; }
                @Override public BitrixResponse<Boolean> sendTyping(io.github.bitrix24.bots.api.model.chat.SendTypingRequest r) { return BitrixResponse.ok(true); }
            };
        }

        @Override
        public CommandApi command() {
            return new CommandApi() {
                @Override
                public BitrixResponse<CommandRegisterResult> register(CommandRegisterRequest request) {
                    return BitrixResponse.ok(CommandRegisterResult.builder()
                            .command(io.github.bitrix24.bots.api.model.command.Command.builder().id(888L).build())
                            .build());
                }
                @Override public BitrixResponse<Boolean> unregister(io.github.bitrix24.bots.api.model.command.CommandUnregisterRequest r) { return BitrixResponse.ok(true); }
                @Override
                public BitrixResponse<Boolean> answer(CommandAnswerRequest request) {
                    recordedAnswers.add(request);
                    return BitrixResponse.ok(true);
                }
            };
        }

        @Override
        public EventApi event() {
            return request -> {
                if (!queuedEvents.isEmpty()) {
                    return BitrixResponse.ok(queuedEvents.remove(0));
                }
                return BitrixResponse.ok(EventGetResult.builder().build());
            };
        }

        @Override public FileApi file() { return null; }

        @Override public <T> BitrixResponse<T> callMethod(String m, Object p, Class<T> r) { return null; }
        @Override public <T> BitrixResponse<T> callMethod(String m, Object p, TypeReference<T> r) { return null; }
        @Override public <T> BitrixResponse<T> callMethod(String m, Object p, JavaType r) { return null; }
        @Override public BitrixResponse<BatchResult> batch(BatchRequest request) { return null; }
        @Override public void close() {}
    }
}
