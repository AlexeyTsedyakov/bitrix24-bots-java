package io.github.bitrix24.bots.api.json;

import io.github.bitrix24.bots.api.exception.BitrixApiException;
import io.github.bitrix24.bots.api.model.common.BitrixErrorResponse;
import io.github.bitrix24.bots.api.model.common.BitrixResponse;
import io.github.bitrix24.bots.api.model.common.BitrixTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BitrixJsonMapperTest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class DummyBotPayload {
        private Long id;
        private String botCode;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class ImbotV2RequestPayload {
        private String botToken;
        private String eventMode;
        private Long dialogId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class ImbotV2EventGetResult {
        private Long nextOffset;
        private Boolean hasMore;
        private Boolean hasNextPage;
    }

    @Test
    @DisplayName("BitrixTime deserializes accurately from Bitrix24 REST API time object including rate limit fields")
    void testBitrixTimeDeserialization() {
        String json = """
                {
                    "start": 1728626400.123,
                    "finish": 1728626400.234,
                    "duration": 0.111,
                    "processing": 0.045,
                    "operating": 0.012,
                    "operating_reset_at": 1728626500,
                    "date_start": "2024-10-11T10:00:00+03:00",
                    "date_finish": "2024-10-11T10:00:00+03:00"
                }
                """;

        BitrixTime time = BitrixJsonMapper.fromJson(json, BitrixTime.class);
        assertThat(time).isNotNull();
        assertThat(time.getStart()).isEqualTo(1728626400.123);
        assertThat(time.getFinish()).isEqualTo(1728626400.234);
        assertThat(time.getDuration()).isEqualTo(0.111);
        assertThat(time.getProcessing()).isEqualTo(0.045);
        assertThat(time.getOperating()).isEqualTo(0.012);
        assertThat(time.getOperatingResetAt()).isEqualTo(1728626500L);

        assertThat(time.getDateStart()).isEqualTo(
                OffsetDateTime.of(2024, 10, 11, 10, 0, 0, 0, ZoneOffset.ofHours(3))
        );
        assertThat(time.getDateFinish()).isEqualTo(
                OffsetDateTime.of(2024, 10, 11, 10, 0, 0, 0, ZoneOffset.ofHours(3))
        );
        assertThat(time.getStartInstant()).isEqualTo(time.getDateStart().toInstant());
        assertThat(time.getFinishInstant()).isEqualTo(time.getDateFinish().toInstant());
    }

    @Test
    @DisplayName("BitrixErrorResponse deserializes standard error JSON")
    void testBitrixErrorResponse() {
        String json = """
                {
                    "error": "BOT_NOT_FOUND",
                    "error_description": "Bot not found",
                    "error_information": "Detailed trace"
                }
                """;

        BitrixErrorResponse error = BitrixJsonMapper.fromJson(json, BitrixErrorResponse.class);
        assertThat(error).isNotNull();
        assertThat(error.getError()).isEqualTo("BOT_NOT_FOUND");
        assertThat(error.getErrorDescription()).isEqualTo("Bot not found");
        assertThat(error.getErrorInformation()).isEqualTo("Detailed trace");
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class NumberNameDto {
        private Integer number;
        private String name;
    }

    @Test
    @DisplayName("BitrixResponse<T> parses successfully with typed payload and timing")
    void testBitrixResponseSuccess() {
        String json = """
                {
                    "result": {
                        "id": 456,
                        "botCode": "my_bot",
                        "isActive": true
                    },
                    "time": {
                        "duration": 0.05
                    },
                    "total": 1,
                    "next": 0
                }
                """;

        BitrixResponse<DummyBotPayload> response = BitrixJsonMapper.parseResponse(json, DummyBotPayload.class);
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.isError()).isFalse();
        assertThat(response.getTotal()).isEqualTo(1);
        assertThat(response.getNext()).isEqualTo(0);

        DummyBotPayload payload = response.getResultOrThrow();
        assertThat(payload.getId()).isEqualTo(456L);
        assertThat(payload.getBotCode()).isEqualTo("my_bot");
        assertThat(payload.getIsActive()).isTrue();
        assertThat(response.getTime().getDuration()).isEqualTo(0.05);
    }

    @Test
    @DisplayName("imbot.v2 wire format preserves camelCase in serialization and deserialization")
    void testImbotV2CamelCaseContract() {
        // Serialization: verify request fields are kept in camelCase
        ImbotV2RequestPayload request = ImbotV2RequestPayload.builder()
                .botToken("my_bot_token_123")
                .eventMode("fetch")
                .dialogId(789L)
                .build();

        String json = BitrixJsonMapper.toJson(request);
        assertThat(json).contains("\"botToken\":\"my_bot_token_123\"");
        assertThat(json).contains("\"eventMode\":\"fetch\"");
        assertThat(json).contains("\"dialogId\":789");
        assertThat(json).doesNotContain("\"bot_token\":");
        assertThat(json).doesNotContain("\"event_mode\":");
        assertThat(json).doesNotContain("\"dialog_id\":");

        // Deserialization: imbot.v2 results with camelCase fields (hasNextPage, nextOffset, hasMore)
        String responseJson = """
                {
                    "result": {
                        "nextOffset": 100,
                        "hasMore": true,
                        "hasNextPage": true
                    }
                }
                """;

        BitrixResponse<ImbotV2EventGetResult> response =
                BitrixJsonMapper.parseResponse(responseJson, ImbotV2EventGetResult.class);
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();

        ImbotV2EventGetResult result = response.getResultOrThrow();
        assertThat(result.getNextOffset()).isEqualTo(100L);
        assertThat(result.getHasMore()).isTrue();
        assertThat(result.getHasNextPage()).isTrue();
    }

    @Test
    @DisplayName("BitrixResponse<T> with error throws BitrixApiException on getResultOrThrow() preserving errorInformation")
    void testBitrixResponseError() {
        String json = """
                {
                    "error": "AUTH_ERROR",
                    "error_description": "Wrong authorization token",
                    "error_information": "Trace info 123"
                }
                """;

        BitrixResponse<DummyBotPayload> response = BitrixJsonMapper.parseResponse(json, DummyBotPayload.class);
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.isError()).isTrue();
        assertThat(response.getError()).isEqualTo("AUTH_ERROR");
        assertThat(response.getErrorDescription()).isEqualTo("Wrong authorization token");
        assertThat(response.getErrorInformation()).isEqualTo("Trace info 123");

        assertThatThrownBy(response::getResultOrThrow)
                .isInstanceOf(BitrixApiException.class)
                .satisfies(ex -> {
                    BitrixApiException apiEx = (BitrixApiException) ex;
                    assertThat(apiEx.getError()).isEqualTo("AUTH_ERROR");
                    assertThat(apiEx.getErrorDescription()).isEqualTo("Wrong authorization token");
                    assertThat(apiEx.getErrorInformation()).isEqualTo("Trace info 123");
                    assertThat(apiEx.getMessage()).contains("[AUTH_ERROR] Wrong authorization token");
                });
    }

    @Test
    @DisplayName("BitrixJsonMapper ignores unknown properties")
    void testUnknownPropertiesIgnored() {
        String json = """
                {
                    "id": 123,
                    "botCode": "tester",
                    "isActive": false,
                    "new_future_property": "some_value"
                }
                """;

        DummyBotPayload payload = BitrixJsonMapper.fromJson(json, DummyBotPayload.class);
        assertThat(payload).isNotNull();
        assertThat(payload.getId()).isEqualTo(123L);
        assertThat(payload.getBotCode()).isEqualTo("tester");
    }

    @Test
    @DisplayName("BitrixJsonMapper handles snake_case serialization via createSnakeCaseMapper")
    void testSerializationSnakeCase() {
        DummyBotPayload payload = DummyBotPayload.builder()
                .id(789L)
                .botCode("support_agent")
                .isActive(true)
                .build();

        String json = BitrixJsonMapper.createSnakeCaseMapper().writeValueAsString(payload);
        assertThat(json).contains("\"bot_code\":\"support_agent\"");
        assertThat(json).contains("\"is_active\":true");
    }

    @Test
    @DisplayName("BitrixJsonMapper readTree and convertValue work properly")
    void testReadTreeAndConvertValue() {
        String json = "{\"number\": 42, \"name\": \"test\"}";
        JsonNode node = BitrixJsonMapper.readTree(json);
        assertThat(node.get("number").asInt()).isEqualTo(42);
        assertThat(node.get("name").asString()).isEqualTo("test");

        NumberNameDto dto = BitrixJsonMapper.convertValue(node, NumberNameDto.class);
        assertThat(dto).isNotNull();
        assertThat(dto.getNumber()).isEqualTo(42);
        assertThat(dto.getName()).isEqualTo("test");
    }

    @Test
    @DisplayName("BitrixJsonMapper supports case-insensitive matching and string-to-number coercion")
    void testCoercionAndCaseInsensitivity() {
        // Coercion from string to numbers/booleans as often occurs in Bitrix24 PHP webhook payloads
        String json = """
                {
                    "ID": "123",
                    "botcode": "tester_bot",
                    "ISACTIVE": "true"
                }
                """;

        DummyBotPayload payload = BitrixJsonMapper.fromJson(json, DummyBotPayload.class);
        assertThat(payload).isNotNull();
        assertThat(payload.getId()).isEqualTo(123L);
        assertThat(payload.getBotCode()).isEqualTo("tester_bot");
        assertThat(payload.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("BitrixJsonMapper coerces webhook boolean strings ('1', '0', 'Y', 'N', '') and empty scalar strings")
    void testWebhookScalarStringCoercion() {
        DummyBotPayload activeOne = BitrixJsonMapper.fromJson("{\"id\":\"789\",\"isActive\":\"1\"}", DummyBotPayload.class);
        assertThat(activeOne.getId()).isEqualTo(789L);
        assertThat(activeOne.getIsActive()).isTrue();

        DummyBotPayload activeZero = BitrixJsonMapper.fromJson("{\"id\":\"789\",\"isActive\":\"0\"}", DummyBotPayload.class);
        assertThat(activeZero.getIsActive()).isFalse();

        DummyBotPayload activeYes = BitrixJsonMapper.fromJson("{\"isActive\":\"Y\"}", DummyBotPayload.class);
        assertThat(activeYes.getIsActive()).isTrue();

        DummyBotPayload activeNo = BitrixJsonMapper.fromJson("{\"isActive\":\"N\"}", DummyBotPayload.class);
        assertThat(activeNo.getIsActive()).isFalse();

        DummyBotPayload emptyScalars = BitrixJsonMapper.fromJson("{\"id\":\"\",\"isActive\":\"\"}", DummyBotPayload.class);
        assertThat(emptyScalars.getId()).isNull();
        assertThat(emptyScalars.getIsActive()).isNull();
    }
}

