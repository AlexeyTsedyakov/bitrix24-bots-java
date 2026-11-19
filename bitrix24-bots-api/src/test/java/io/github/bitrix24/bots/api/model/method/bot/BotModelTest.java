package io.github.bitrix24.bots.api.model.method.bot;

import io.github.bitrix24.bots.api.json.BitrixJsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BotModelTest {

    @Test
    @DisplayName("Tests method.bot.BotGetRequest serialization, factory methods, and botToken masking")
    void testBotGetRequestSerialization() {
        BotGetRequest request = BotGetRequest.builder()
                .botId(456L)
                .code("support_bot")
                .botToken("secret_bot_token")
                .build();

        assertThat(request.toString()).doesNotContain("secret_bot_token");

        String json = BitrixJsonMapper.toJson(request);
        assertThat(json)
                .contains("\"botId\":456")
                .contains("\"code\":\"support_bot\"")
                .contains("\"botToken\":\"secret_bot_token\"");

        BotGetRequest byId = BotGetRequest.ofId(456L);
        assertThat(byId.getBotId()).isEqualTo(456L);
        assertThat(byId.getCode()).isNull();

        BotGetRequest byCode = BotGetRequest.ofCode("support_bot");
        assertThat(byCode.getCode()).isEqualTo("support_bot");
        assertThat(byCode.getBotId()).isNull();
    }

    @Test
    @DisplayName("Tests method.bot.BotGetResult deserialization against imbot.v2.Bot.get example")
    void testBotGetResultDeserialization() {
        String json = """
                {
                    "bot": {
                        "id": 456,
                        "code": "support_bot",
                        "type": "bot",
                        "isHidden": false,
                        "isSupportOpenline": false,
                        "isReactionsEnabled": true,
                        "backgroundId": null,
                        "language": "ru",
                        "moduleId": "rest",
                        "eventMode": "fetch",
                        "countMessage": 150,
                        "countCommand": 3,
                        "countChat": 12,
                        "countUser": 45
                    },
                    "users": [
                        {
                            "id": 456,
                            "active": true,
                            "name": "Support Bot",
                            "bot": true,
                            "type": "bot"
                        }
                    ]
                }
                """;

        BotGetResult result = BitrixJsonMapper.fromJson(json, BotGetResult.class);
        assertThat(result).isNotNull();
        assertThat(result.getBot()).isNotNull();
        assertThat(result.getBot().getId()).isEqualTo(456L);
        assertThat(result.getBot().getCode()).isEqualTo("support_bot");
        assertThat(result.getBot().getType()).isEqualTo("bot");
        assertThat(result.getBot().getIsReactionsEnabled()).isTrue();
        assertThat(result.getBot().getEventMode()).isEqualTo("fetch");
        assertThat(result.getBot().getCountMessage()).isEqualTo(150);
        assertThat(result.getUsers()).hasSize(1);
        assertThat(result.getUsers().get(0).getId()).isEqualTo(456L);
        assertThat(result.getUsers().get(0).getName()).isEqualTo("Support Bot");
        assertThat(result.getUsers().get(0).getBot()).isTrue();
    }

    @Test
    @DisplayName("Tests method.bot.BotListRequest serialization, factory method, and botToken masking")
    void testBotListRequestSerialization() {
        BotListRequest request = BotListRequest.builder()
                .botToken("secret_bot_token")
                .filter(java.util.Map.of("type", "bot"))
                .limit(10)
                .offset(20)
                .build();

        assertThat(request.toString()).doesNotContain("secret_bot_token");

        String json = BitrixJsonMapper.toJson(request);
        assertThat(json)
                .contains("\"botToken\":\"secret_bot_token\"")
                .contains("\"filter\":{\"type\":\"bot\"}")
                .contains("\"limit\":10")
                .contains("\"offset\":20");

        BotListRequest filtered = BotListRequest.ofFilter(java.util.Map.of("type", "supervisor"));
        assertThat(filtered.getFilter()).containsEntry("type", "supervisor");
        assertThat(filtered.getLimit()).isNull();
        assertThat(filtered.getOffset()).isNull();
    }

    @Test
    @DisplayName("Tests method.bot.BotListResult deserialization against imbot.v2.Bot.list example")
    void testBotListResultDeserialization() {
        String json = """
                {
                    "bots": [
                        {
                            "id": 456,
                            "code": "support_bot",
                            "type": "bot",
                            "isHidden": false,
                            "isSupportOpenline": false,
                            "isReactionsEnabled": true,
                            "backgroundId": null,
                            "language": "ru",
                            "moduleId": "rest",
                            "eventMode": "fetch",
                            "countMessage": 150,
                            "countCommand": 3,
                            "countChat": 12,
                            "countUser": 45
                        }
                    ],
                    "users": [
                        {
                            "id": 456,
                            "active": true,
                            "name": "Support Bot",
                            "bot": true,
                            "type": "bot"
                        }
                    ],
                    "hasNextPage": false
                }
                """;

        BotListResult result = BitrixJsonMapper.fromJson(json, BotListResult.class);
        assertThat(result).isNotNull();
        assertThat(result.getBots()).hasSize(1);
        assertThat(result.getBots().get(0).getId()).isEqualTo(456L);
        assertThat(result.getBots().get(0).getCode()).isEqualTo("support_bot");
        assertThat(result.getUsers()).hasSize(1);
        assertThat(result.getUsers().get(0).getName()).isEqualTo("Support Bot");
        assertThat(result.getHasNextPage()).isFalse();
    }

    @Test
    @DisplayName("Tests method.bot.BotProperties serialization and deserialization")
    void testBotPropertiesSerialization() {
        BotProperties properties = BotProperties.builder()
                .name("Support Bot")
                .lastName("Assistant")
                .workPosition("AI Assistant")
                .color("AQUA")
                .gender("M")
                .avatar("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=")
                .build();

        String json = BitrixJsonMapper.toJson(properties);
        assertThat(json)
                .contains("\"name\":\"Support Bot\"")
                .contains("\"lastName\":\"Assistant\"")
                .contains("\"workPosition\":\"AI Assistant\"")
                .contains("\"color\":\"AQUA\"")
                .contains("\"gender\":\"M\"")
                .contains("\"avatar\":\"iVBORw0KGgo");

        BotProperties deserialized = BitrixJsonMapper.fromJson(json, BotProperties.class);
        assertThat(deserialized).isEqualTo(properties);
    }

    @Test
    @DisplayName("Tests method.bot.BotRegisterFields serialization, deserialization, and botToken masking")
    void testBotRegisterFieldsSerialization() {
        BotRegisterFields fields = BotRegisterFields.builder()
                .code("support_bot")
                .botToken("secret_bot_token")
                .properties(BotProperties.builder().name("Support Bot").workPosition("AI Assistant").build())
                .type("bot")
                .eventMode("webhook")
                .webhookUrl("https://example.com/webhook")
                .isHidden(false)
                .isReactionsEnabled(true)
                .isSupportOpenline(false)
                .backgroundId("azure")
                .build();

        assertThat(fields.toString()).doesNotContain("secret_bot_token");

        String json = BitrixJsonMapper.toJson(fields);
        assertThat(json)
                .contains("\"code\":\"support_bot\"")
                .contains("\"botToken\":\"secret_bot_token\"")
                .contains("\"properties\"")
                .contains("\"type\":\"bot\"")
                .contains("\"eventMode\":\"webhook\"")
                .contains("\"webhookUrl\":\"https://example.com/webhook\"")
                .contains("\"isHidden\":false")
                .contains("\"isReactionsEnabled\":true")
                .contains("\"isSupportOpenline\":false")
                .contains("\"backgroundId\":\"azure\"");

        BotRegisterFields deserialized = BitrixJsonMapper.fromJson(json, BotRegisterFields.class);
        assertThat(deserialized).isEqualTo(fields);
    }

    @Test
    @DisplayName("Tests method.bot.BotRegisterRequest serialization and factory method")
    void testBotRegisterRequestSerialization() {
        BotRegisterFields fields = BotRegisterFields.builder()
                .code("support_bot")
                .botToken("my_bot_token")
                .properties(BotProperties.builder().name("Support Bot").workPosition("AI Assistant").build())
                .type("bot")
                .eventMode("fetch")
                .build();

        BotRegisterRequest request = BotRegisterRequest.of(fields);
        String json = BitrixJsonMapper.toJson(request);
        assertThat(json)
                .contains("\"fields\":{")
                .contains("\"code\":\"support_bot\"")
                .contains("\"botToken\":\"my_bot_token\"")
                .contains("\"type\":\"bot\"")
                .contains("\"eventMode\":\"fetch\"");

        BotRegisterRequest deserialized = BitrixJsonMapper.fromJson(json, BotRegisterRequest.class);
        assertThat(deserialized).isEqualTo(request);
    }

    @Test
    @DisplayName("Tests method.bot.BotRegisterResult deserialization against imbot.v2.Bot.register example")
    void testBotRegisterResultDeserialization() {
        String json = """
                {
                    "bot": {
                        "id": 456,
                        "code": "support_bot",
                        "type": "bot",
                        "isHidden": false,
                        "isSupportOpenline": false,
                        "isReactionsEnabled": true,
                        "backgroundId": null,
                        "language": "en",
                        "moduleId": "rest",
                        "eventMode": "fetch",
                        "countMessage": 0,
                        "countCommand": 0,
                        "countChat": 0,
                        "countUser": 0
                    },
                    "users": [
                        {
                            "id": 456,
                            "active": true,
                            "name": "Support Bot",
                            "firstName": "Support",
                            "lastName": "Bot",
                            "workPosition": "AI Assistant",
                            "color": "#df532d",
                            "avatar": "",
                            "gender": "M",
                            "birthday": "",
                            "extranet": false,
                            "bot": true,
                            "connector": false,
                            "externalAuthId": "bot",
                            "status": "online",
                            "idle": false,
                            "lastActivityDate": "2025-01-15T10:30:00+03:00",
                            "absent": false,
                            "departments": [1],
                            "phones": false,
                            "type": "bot"
                        }
                    ]
                }
                """;

        BotRegisterResult result = BitrixJsonMapper.fromJson(json, BotRegisterResult.class);
        assertThat(result).isNotNull();
        assertThat(result.getBot()).isNotNull();
        assertThat(result.getBot().getId()).isEqualTo(456L);
        assertThat(result.getBot().getCode()).isEqualTo("support_bot");
        assertThat(result.getUsers()).hasSize(1);
        assertThat(result.getUsers().get(0).getId()).isEqualTo(456L);
        assertThat(result.getUsers().get(0).getWorkPosition()).isEqualTo("AI Assistant");
    }

    @Test
    @DisplayName("Tests method.bot.BotUpdateFields serialization, deserialization, and botToken masking")
    void testBotUpdateFieldsSerialization() {
        BotUpdateFields fields = BotUpdateFields.builder()
                .properties(BotProperties.builder().name("Updated Bot").workPosition("AI Assistant").build())
                .isHidden(true)
                .isReactionsEnabled(false)
                .isSupportOpenline(true)
                .backgroundId("mint")
                .eventMode("webhook")
                .webhookUrl("https://example.com/updated-webhook")
                .botToken("rotated_bot_token")
                .build();

        assertThat(fields.toString()).doesNotContain("rotated_bot_token");

        String json = BitrixJsonMapper.toJson(fields);
        assertThat(json)
                .contains("\"properties\"")
                .contains("\"isHidden\":true")
                .contains("\"isReactionsEnabled\":false")
                .contains("\"isSupportOpenline\":true")
                .contains("\"backgroundId\":\"mint\"")
                .contains("\"eventMode\":\"webhook\"")
                .contains("\"webhookUrl\":\"https://example.com/updated-webhook\"")
                .contains("\"botToken\":\"rotated_bot_token\"");

        BotUpdateFields deserialized = BitrixJsonMapper.fromJson(json, BotUpdateFields.class);
        assertThat(deserialized).isEqualTo(fields);
    }

    @Test
    @DisplayName("Tests method.bot.BotUpdateRequest serialization, factory method, and botToken masking")
    void testBotUpdateRequestSerialization() {
        BotUpdateFields fields = BotUpdateFields.builder()
                .properties(BotProperties.builder().name("Updated Bot").build())
                .isHidden(true)
                .build();

        BotUpdateRequest request = BotUpdateRequest.builder()
                .botId(456L)
                .botToken("secret_bot_token")
                .fields(fields)
                .build();

        assertThat(request.toString()).doesNotContain("secret_bot_token");

        String json = BitrixJsonMapper.toJson(request);
        assertThat(json)
                .contains("\"botId\":456")
                .contains("\"botToken\":\"secret_bot_token\"")
                .contains("\"fields\":{")
                .contains("\"isHidden\":true");

        BotUpdateRequest deserialized = BitrixJsonMapper.fromJson(json, BotUpdateRequest.class);
        assertThat(deserialized).isEqualTo(request);

        BotUpdateRequest factoryRequest = BotUpdateRequest.of(456L, fields);
        assertThat(factoryRequest.getBotId()).isEqualTo(456L);
        assertThat(factoryRequest.getFields()).isEqualTo(fields);
        assertThat(factoryRequest.getBotToken()).isNull();
    }

    @Test
    @DisplayName("Tests method.bot.BotUpdateResult deserialization against imbot.v2.Bot.update example")
    void testBotUpdateResultDeserialization() {
        String json = """
                {
                    "bot": {
                        "id": 456,
                        "code": "support_bot",
                        "type": "bot",
                        "isHidden": true,
                        "isSupportOpenline": false,
                        "isReactionsEnabled": true,
                        "backgroundId": null,
                        "language": "ru",
                        "moduleId": "rest",
                        "eventMode": "fetch",
                        "countMessage": 150,
                        "countCommand": 3,
                        "countChat": 12,
                        "countUser": 45
                    },
                    "users": [
                        {
                            "id": 456,
                            "active": true,
                            "name": "Updated Bot",
                            "bot": true,
                            "type": "bot"
                        }
                    ]
                }
                """;

        BotUpdateResult result = BitrixJsonMapper.fromJson(json, BotUpdateResult.class);
        assertThat(result).isNotNull();
        assertThat(result.getBot()).isNotNull();
        assertThat(result.getBot().getId()).isEqualTo(456L);
        assertThat(result.getBot().getIsHidden()).isTrue();
        assertThat(result.getUsers()).hasSize(1);
        assertThat(result.getUsers().get(0).getId()).isEqualTo(456L);
        assertThat(result.getUsers().get(0).getName()).isEqualTo("Updated Bot");
    }

    @Test
    @DisplayName("Tests method.bot.BotUnregisterRequest serialization, factory method, and botToken masking")
    void testBotUnregisterRequestSerialization() {
        BotUnregisterRequest request = BotUnregisterRequest.builder()
                .botId(456L)
                .botToken("secret_bot_token")
                .build();

        assertThat(request.toString()).doesNotContain("secret_bot_token");

        String json = BitrixJsonMapper.toJson(request);
        assertThat(json)
                .contains("\"botId\":456")
                .contains("\"botToken\":\"secret_bot_token\"");

        BotUnregisterRequest deserialized = BitrixJsonMapper.fromJson(json, BotUnregisterRequest.class);
        assertThat(deserialized).isEqualTo(request);

        BotUnregisterRequest factoryRequest = BotUnregisterRequest.of(456L);
        assertThat(factoryRequest.getBotId()).isEqualTo(456L);
        assertThat(factoryRequest.getBotToken()).isNull();
    }

    @Test
    @DisplayName("Tests method.bot.BotUnregisterResult deserialization against imbot.v2.Bot.unregister example")
    void testBotUnregisterResultDeserialization() {
        String json = """
                {
                    "result": true
                }
                """;

        BotUnregisterResult result = BitrixJsonMapper.fromJson(json, BotUnregisterResult.class);
        assertThat(result).isNotNull();
        assertThat(result.getResult()).isTrue();
    }
}
