package io.github.bitrix24.bots.api.client;

import io.github.bitrix24.bots.api.exception.BitrixApiException;
import io.github.bitrix24.bots.api.model.bot.*;
import io.github.bitrix24.bots.api.model.chat.ReadMessageResult;
import io.github.bitrix24.bots.api.model.chat.SendMessageFields;
import io.github.bitrix24.bots.api.model.chat.SendMessageRequest;
import io.github.bitrix24.bots.api.model.chat.SendMessageResult;
import io.github.bitrix24.bots.api.model.chat.attach.Attach;
import io.github.bitrix24.bots.api.model.chat.keyboard.Keyboard;
import io.github.bitrix24.bots.api.model.chat.keyboard.KeyboardButton;
import io.github.bitrix24.bots.api.model.command.CommandRegisterResult;
import io.github.bitrix24.bots.api.model.common.BitrixResponse;
import io.github.bitrix24.bots.api.model.common.batch.BatchResult;
import io.github.bitrix24.bots.api.model.event.EventGetResult;
import io.github.bitrix24.bots.api.model.event.EventType;
import io.github.bitrix24.bots.api.model.file.FileDownloadResult;
import io.github.bitrix24.bots.api.model.file.FileUploadResult;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BitrixApiClientTest {

    private MockWebServer server;
    private BitrixApiClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();

        BitrixApiConfig config = BitrixApiConfig.builder()
                .baseUrl(server.url("/rest/1/secret_key/").toString())
                .botId(456L)
                .botToken("my_bot_token")
                .applicationToken("app_token_123")
                .build();

        client = BitrixApiClient.create(config);
    }

    @AfterEach
    void tearDown() throws IOException {
        client.close();
        server.shutdown();
    }

    @Test
    @DisplayName("Should register bot via imbot.v2.Bot.register")
    void testBotRegister() throws InterruptedException {
        String jsonResponse = """
                {
                    "result": {
                        "bot": {
                            "id": 456,
                            "code": "support_bot",
                            "type": "bot",
                            "isHidden": false,
                            "eventMode": "fetch"
                        },
                        "users": [
                            {"id": 456, "name": "Support Bot", "active": true}
                        ]
                    },
                    "time": {
                        "start": 1728626400.123,
                        "finish": 1728626400.234,
                        "duration": 0.111
                    }
                }
                """;
        server.enqueue(new MockResponse().setBody(jsonResponse).setHeader("Content-Type", "application/json"));

        BotRegisterFields fields = BotRegisterFields.builder()
                .code("support_bot")
                .properties(BotProperties.builder().name("Support Bot").workPosition("Assistant").build())
                .eventMode("fetch")
                .build();

        BitrixResponse<BotRegisterResult> response = client.bot().register(fields);

        assertThat(response.isSuccess()).isTrue();
        BotRegisterResult result = response.getResultOrThrow();
        assertThat(result.getBot().getId()).isEqualTo(456L);
        assertThat(result.getBot().getCode()).isEqualTo("support_bot");
        assertThat(result.getUsers()).hasSize(1);
        assertThat(result.getUsers().get(0).getName()).isEqualTo("Support Bot");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/rest/1/secret_key/imbot.v2.Bot.register");
        String body = req.getBody().readUtf8();
        assertThat(body).contains("\"code\":\"support_bot\"");
        assertThat(body).contains("\"botToken\":\"my_bot_token\""); // auto-enriched from config
    }

    @Test
    @DisplayName("Should update bot via imbot.v2.Bot.update")
    void testBotUpdate() throws InterruptedException {
        String jsonResponse = """
                {
                    "result": {
                        "bot": {"id": 456, "code": "support_bot", "isHidden": true},
                        "users": []
                    }
                }
                """;
        server.enqueue(new MockResponse().setBody(jsonResponse).setHeader("Content-Type", "application/json"));

        BitrixResponse<BotUpdateResult> response = client.bot().update(456L, BotUpdateFields.builder().isHidden(true).build());

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResultOrThrow().getBot().getIsHidden()).isTrue();

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/rest/1/secret_key/imbot.v2.Bot.update");
        assertThat(req.getBody().readUtf8()).contains("\"botId\":456");
    }

    @Test
    @DisplayName("Should get bot by code or ID via imbot.v2.Bot.get")
    void testBotGet() throws InterruptedException {
        String jsonResponse = """
                {
                    "result": {
                        "bot": {"id": 456, "code": "support_bot"},
                        "users": []
                    }
                }
                """;
        server.enqueue(new MockResponse().setBody(jsonResponse).setHeader("Content-Type", "application/json"));

        BitrixResponse<BotGetResult> response = client.bot().get("support_bot");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResultOrThrow().getBot().getCode()).isEqualTo("support_bot");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/rest/1/secret_key/imbot.v2.Bot.get");
        assertThat(req.getBody().readUtf8()).contains("\"code\":\"support_bot\"");
    }

    @Test
    @DisplayName("Should list bots via imbot.v2.Bot.list")
    void testBotList() throws InterruptedException {
        String jsonResponse = """
                {
                    "result": {
                        "bots": [{"id": 456, "code": "support_bot"}],
                        "users": [],
                        "hasNextPage": false
                    }
                }
                """;
        server.enqueue(new MockResponse().setBody(jsonResponse).setHeader("Content-Type", "application/json"));

        BitrixResponse<BotListResult> response = client.bot().list();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResultOrThrow().getBots()).hasSize(1);
        assertThat(response.getResultOrThrow().getHasNextPage()).isFalse();

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/rest/1/secret_key/imbot.v2.Bot.list");
    }

    @Test
    @DisplayName("Should unregister bot via imbot.v2.Bot.unregister")
    void testBotUnregister() throws InterruptedException {
        String jsonResponse = "{\"result\": {\"result\": true}}";
        server.enqueue(new MockResponse().setBody(jsonResponse).setHeader("Content-Type", "application/json"));

        BitrixResponse<BotUnregisterResult> response = client.bot().unregister(456L);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResultOrThrow().getResult()).isTrue();

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/rest/1/secret_key/imbot.v2.Bot.unregister");
        assertThat(req.getBody().readUtf8()).contains("\"botId\":456");
    }

    @Test
    @DisplayName("Should send message with keyboard and attach via imbot.v2.Chat.Message.send")
    void testMessageSend() throws InterruptedException {
        String jsonResponse = """
                {
                    "result": {
                        "id": 789,
                        "uuidMap": {},
                        "failureMap": {}
                    }
                }
                """;
        server.enqueue(new MockResponse().setBody(jsonResponse).setHeader("Content-Type", "application/json"));

        Keyboard keyboard = Keyboard.of(
                KeyboardButton.link("Website", "https://example.com"),
                KeyboardButton.put("Help", "/help")
        );
        Attach attach = Attach.builder().colorToken("primary").build().addMessage("Attached card");

        SendMessageRequest request = SendMessageRequest.builder()
                .dialogId("chat5")
                .fields(SendMessageFields.builder()
                        .message("Hello world!")
                        .keyboard(keyboard)
                        .attach(attach)
                        .replyId(100L)
                        .build())
                .build();

        BitrixResponse<SendMessageResult> response = client.message().send(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResultOrThrow().getId()).isEqualTo(789L);

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/rest/1/secret_key/imbot.v2.Chat.Message.send");
        String body = req.getBody().readUtf8();
        assertThat(body).contains("\"dialogId\":\"chat5\"");
        assertThat(body).contains("\"TEXT\":\"Website\"");
        assertThat(body).contains("\"COLOR_TOKEN\":\"primary\"");
    }

    @Test
    @DisplayName("Should update, delete and read messages via imbot.v2.Chat.Message.*")
    void testMessageUpdateDeleteRead() throws InterruptedException {
        // 1. update
        server.enqueue(new MockResponse().setBody("{\"result\": {\"result\": true}}").setHeader("Content-Type", "application/json"));
        BitrixResponse<Boolean> updateResp = client.message().update(789L, "Updated message");
        assertThat(updateResp.isSuccess()).isTrue();
        assertThat(updateResp.getResultOrThrow()).isTrue();

        RecordedRequest req1 = server.takeRequest();
        assertThat(req1.getPath()).isEqualTo("/rest/1/secret_key/imbot.v2.Chat.Message.update");
        assertThat(req1.getBody().readUtf8()).contains("\"messageId\":789");

        // 2. delete
        server.enqueue(new MockResponse().setBody("{\"result\": {\"result\": true}}").setHeader("Content-Type", "application/json"));
        BitrixResponse<Boolean> delResp = client.message().delete(789L);
        assertThat(delResp.isSuccess()).isTrue();
        assertThat(delResp.getResultOrThrow()).isTrue();

        RecordedRequest req2 = server.takeRequest();
        assertThat(req2.getPath()).isEqualTo("/rest/1/secret_key/imbot.v2.Chat.Message.delete");

        // 3. read
        server.enqueue(new MockResponse().setBody("""
                {
                    "result": {
                        "chatId": 5,
                        "lastId": 789,
                        "counter": 0,
                        "viewedMessages": [785, 786, 789]
                    }
                }
                """).setHeader("Content-Type", "application/json"));
        BitrixResponse<ReadMessageResult> readResp = client.message().read("chat5", 789L);
        assertThat(readResp.isSuccess()).isTrue();
        assertThat(readResp.getResultOrThrow().getChatId()).isEqualTo(5L);
        assertThat(readResp.getResultOrThrow().getViewedMessages()).containsExactly(785L, 786L, 789L);

        RecordedRequest req3 = server.takeRequest();
        assertThat(req3.getPath()).isEqualTo("/rest/1/secret_key/imbot.v2.Chat.Message.read");

        // 4. sendTyping
        server.enqueue(new MockResponse().setBody("{\"result\": {\"result\": true}}").setHeader("Content-Type", "application/json"));
        BitrixResponse<Boolean> typingResp = client.message().sendTyping("chat5", 15);
        assertThat(typingResp.isSuccess()).isTrue();
        assertThat(typingResp.getResultOrThrow()).isTrue();

        RecordedRequest req4 = server.takeRequest();
        assertThat(req4.getPath()).isEqualTo("/rest/1/secret_key/imbot.v2.Chat.InputAction.notify");
        assertThat(req4.getBody().readUtf8()).contains("\"duration\":15");
    }

    @Test
    @DisplayName("Should register, unregister and answer commands via imbot.v2.Command.*")
    void testCommandMethods() throws InterruptedException {
        // 1. register
        server.enqueue(new MockResponse().setBody("""
                {
                    "result": {
                        "command": {
                            "id": 42,
                            "botId": 456,
                            "command": "/help",
                            "common": false,
                            "hidden": false
                        }
                    }
                }
                """).setHeader("Content-Type", "application/json"));

        BitrixResponse<CommandRegisterResult> regResp = client.command().register("help", "Help Command");
        assertThat(regResp.isSuccess()).isTrue();
        assertThat(regResp.getResultOrThrow().getCommand().getId()).isEqualTo(42L);

        RecordedRequest req1 = server.takeRequest();
        assertThat(req1.getPath()).isEqualTo("/rest/1/secret_key/imbot.v2.Command.register");

        // 2. answer
        server.enqueue(new MockResponse().setBody("{\"result\": {\"result\": true}}").setHeader("Content-Type", "application/json"));
        BitrixResponse<Boolean> ansResp = client.command().answer(42L, 789L, "chat5", "Command response text");
        assertThat(ansResp.isSuccess()).isTrue();
        assertThat(ansResp.getResultOrThrow()).isTrue();

        RecordedRequest req2 = server.takeRequest();
        assertThat(req2.getPath()).isEqualTo("/rest/1/secret_key/imbot.v2.Command.answer");

        // 3. unregister
        server.enqueue(new MockResponse().setBody("{\"result\": {\"result\": true}}").setHeader("Content-Type", "application/json"));
        BitrixResponse<Boolean> unregResp = client.command().unregister(42L);
        assertThat(unregResp.isSuccess()).isTrue();
        assertThat(unregResp.getResultOrThrow()).isTrue();

        RecordedRequest req3 = server.takeRequest();
        assertThat(req3.getPath()).isEqualTo("/rest/1/secret_key/imbot.v2.Command.unregister");
        assertThat(req3.getBody().readUtf8()).contains("\"commandId\":42");
    }

    @Test
    @DisplayName("Should poll event queue via imbot.v2.Event.get")
    void testEventGet() throws InterruptedException {
        String jsonResponse = """
                {
                    "result": {
                        "events": [
                            {
                                "eventId": 1001,
                                "type": "ONIMBOTV2MESSAGEADD",
                                "date": "2025-01-15T10:30:00+03:00",
                                "data": {"message": {"text": "hello"}}
                            }
                        ],
                        "nextOffset": 1002,
                        "hasMore": false
                    }
                }
                """;
        server.enqueue(new MockResponse().setBody(jsonResponse).setHeader("Content-Type", "application/json"));

        BitrixResponse<EventGetResult> response = client.event().get(1000L, 50, true);

        assertThat(response.isSuccess()).isTrue();
        EventGetResult result = response.getResultOrThrow();
        assertThat(result.getEvents()).hasSize(1);
        assertThat(result.getEvents().get(0).getEventId()).isEqualTo(1001L);
        assertThat(result.getEvents().get(0).getType()).isEqualTo(EventType.ONIMBOTV2MESSAGEADD);
        assertThat(result.getEvents().get(0).getMessageText()).isEqualTo("hello");
        assertThat(result.getNextOffset()).isEqualTo(1002L);
        assertThat(result.getHasMore()).isFalse();

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/rest/1/secret_key/imbot.v2.Event.get");
        assertThat(req.getBody().readUtf8()).contains("\"offset\":1000");
    }

    @Test
    @DisplayName("Should execute generic method via callMethod and batch requests")
    void testGenericAndBatch() throws InterruptedException {
        // 1. callMethod
        server.enqueue(new MockResponse().setBody("""
                {
                    "result": {
                        "ID": "1",
                        "NAME": "John",
                        "LAST_NAME": "Doe"
                    }
                }
                """).setHeader("Content-Type", "application/json"));

        BitrixResponse<Map> userResp = client.callMethod("user.current", null, Map.class);
        assertThat(userResp.isSuccess()).isTrue();
        assertThat(userResp.getResultOrThrow().get("NAME")).isEqualTo("John");

        RecordedRequest req1 = server.takeRequest();
        assertThat(req1.getPath()).isEqualTo("/rest/1/secret_key/user.current");

        // 2. batch
        server.enqueue(new MockResponse().setBody("""
                {
                    "result": {
                        "result": {
                            "get_user": {"ID": "1", "NAME": "John"}
                        },
                        "result_error": {},
                        "result_total": {"get_user": 1},
                        "result_next": {},
                        "result_time": {}
                    }
                }
                """).setHeader("Content-Type", "application/json"));

        BitrixResponse<BatchResult> batchResp = client.batch(Map.of("get_user", "user.current"));
        assertThat(batchResp.isSuccess()).isTrue();
        BatchResult batchResult = batchResp.getResultOrThrow();
        assertThat(batchResult.getResult()).containsKey("get_user");
        assertThat(batchResult.getResultTotal()).containsEntry("get_user", 1);

        RecordedRequest req2 = server.takeRequest();
        assertThat(req2.getPath()).isEqualTo("/rest/1/secret_key/batch");
    }

    @Test
    @DisplayName("Should parse Bitrix error and throw BitrixApiException on getResultOrThrow()")
    void testApiErrorResponse() {
        String errorJson = """
                {
                    "error": "EMPTY_MESSAGE",
                    "error_description": "Message is empty",
                    "error_information": "fields.message is required"
                }
                """;
        server.enqueue(new MockResponse().setResponseCode(400).setBody(errorJson).setHeader("Content-Type", "application/json"));

        BitrixResponse<SendMessageResult> response = client.message().send("chat1", "");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.isError()).isTrue();
        assertThat(response.getError()).isEqualTo("EMPTY_MESSAGE");
        assertThat(response.getErrorDescription()).isEqualTo("Message is empty");

        assertThatThrownBy(response::getResultOrThrow)
                .isInstanceOf(BitrixApiException.class)
                .hasMessageContaining("EMPTY_MESSAGE")
                .hasMessageContaining("Message is empty");
    }

    @Test
    @DisplayName("Should send OAuth Bearer authorization header when oauthToken is configured")
    void testOAuthAuthorizationHeader() throws InterruptedException {
        BitrixApiConfig oauthConfig = BitrixApiConfig.builder()
                .baseUrl(server.url("/rest/").toString())
                .oauthToken("test_access_token_xyz")
                .build();

        try (BitrixApiClient oauthClient = BitrixApiClient.create(oauthConfig)) {
            server.enqueue(new MockResponse().setBody("{\"result\": true}").setHeader("Content-Type", "application/json"));
            oauthClient.callMethod("user.current", null, Boolean.class);

            RecordedRequest request = server.takeRequest();
            assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test_access_token_xyz");
        }
    }

    @Test
    @DisplayName("Should upload and download chat files via imbot.v2.File.*")
    void testFileUploadAndDownload() throws InterruptedException {
        // 1. imbot.v2.File.upload
        String uploadJson = """
                {
                    "result": {
                        "file": {
                            "id": 138,
                            "chatId": 5,
                            "name": "report.pdf",
                            "extension": "pdf",
                            "size": 12
                        },
                        "messageId": 123,
                        "chatId": 5,
                        "dialogId": "chat5"
                    }
                }
                """;
        server.enqueue(new MockResponse().setBody(uploadJson).setHeader("Content-Type", "application/json"));

        byte[] contentBytes = "Hello World!".getBytes(StandardCharsets.UTF_8);
        BitrixResponse<FileUploadResult> uploadResp = client.file()
                .uploadBytes("chat5", "report.pdf", contentBytes, "Here is the report");

        assertThat(uploadResp.isSuccess()).isTrue();
        FileUploadResult uploadResult = uploadResp.getResultOrThrow();
        assertThat(uploadResult.getMessageId()).isEqualTo(123L);
        assertThat(uploadResult.getChatId()).isEqualTo(5L);
        assertThat(uploadResult.getDialogId()).isEqualTo("chat5");
        assertThat(uploadResult.getFile()).isNotNull();
        assertThat(uploadResult.getFile().getId()).isEqualTo(138L);
        assertThat(uploadResult.getFile().getName()).isEqualTo("report.pdf");
        assertThat(uploadResult.getFile().getExtension()).isEqualTo("pdf");
        assertThat(uploadResult.getFile().getSize()).isEqualTo(12L);

        RecordedRequest uploadReq = server.takeRequest();
        assertThat(uploadReq.getPath()).isEqualTo("/rest/1/secret_key/imbot.v2.File.upload");
        String uploadBody = uploadReq.getBody().readUtf8();
        assertThat(uploadBody).contains("\"botId\":456");
        assertThat(uploadBody).contains("\"botToken\":\"my_bot_token\"");
        assertThat(uploadBody).contains("\"dialogId\":\"chat5\"");
        assertThat(uploadBody).contains("\"name\":\"report.pdf\"");
        assertThat(uploadBody).contains("\"content\":\"SGVsbG8gV29ybGQh\"");
        assertThat(uploadBody).contains("\"message\":\"Here is the report\"");

        // 2. imbot.v2.File.download
        String downloadJson = """
                {
                    "result": {
                        "downloadUrl": "https://test.bitrix24.ru/rest/download.json?token=imbot%7C123"
                    }
                }
                """;
        server.enqueue(new MockResponse().setBody(downloadJson).setHeader("Content-Type", "application/json"));

        BitrixResponse<FileDownloadResult> downloadResp = client.file().download(138L);
        assertThat(downloadResp.isSuccess()).isTrue();
        assertThat(downloadResp.getResultOrThrow().getDownloadUrl())
                .isEqualTo("https://test.bitrix24.ru/rest/download.json?token=imbot%7C123");

        RecordedRequest downloadReq = server.takeRequest();
        assertThat(downloadReq.getPath()).isEqualTo("/rest/1/secret_key/imbot.v2.File.download");
        String downloadBody = downloadReq.getBody().readUtf8();
        assertThat(downloadBody).contains("\"botId\":456");
        assertThat(downloadBody).contains("\"botToken\":\"my_bot_token\"");
        assertThat(downloadBody).contains("\"fileId\":138");
    }
}
