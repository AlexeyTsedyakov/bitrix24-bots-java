package io.github.bitrix24.bots.api.exception;

import io.github.bitrix24.bots.api.model.common.BitrixErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BitrixApiExceptionTest {

    @Test
    @DisplayName("BitrixApiException formats message with status, error code and description")
    void testMessageFormatting() {
        BitrixApiException ex1 = new BitrixApiException("INVALID_REQUEST", "Bad parameter", 400);
        assertThat(ex1.getMessage()).isEqualTo("HTTP 400 - [INVALID_REQUEST] Bad parameter");
        assertThat(ex1.getError()).isEqualTo("INVALID_REQUEST");
        assertThat(ex1.getErrorDescription()).isEqualTo("Bad parameter");
        assertThat(ex1.getHttpStatusCode()).isEqualTo(400);

        BitrixApiException ex2 = new BitrixApiException("BOT_NOT_FOUND", "Bot does not exist");
        assertThat(ex2.getMessage()).isEqualTo("[BOT_NOT_FOUND] Bot does not exist");
        assertThat(ex2.getHttpStatusCode()).isEqualTo(0);

        BitrixApiException ex3 = new BitrixApiException(null, "Gateway timeout", 504);
        assertThat(ex3.getMessage()).isEqualTo("HTTP 504 - Gateway timeout");

        BitrixApiException ex4 = new BitrixApiException("OVERLOAD_LIMIT", (String) null);
        assertThat(ex4.getMessage()).isEqualTo("[OVERLOAD_LIMIT]");

        BitrixApiException ex5 = new BitrixApiException(null, null, 502);
        assertThat(ex5.getMessage()).isEqualTo("HTTP 502 - Bitrix24 API request failed");

        BitrixApiException ex6 = new BitrixApiException("Direct failure message");
        assertThat(ex6.getMessage()).isEqualTo("Direct failure message");
    }

    @Test
    @DisplayName("BitrixApiException constructs correctly from BitrixErrorResponse including null errorResponse")
    void testConstructFromBitrixErrorResponse() {
        BitrixErrorResponse errorResponse = BitrixErrorResponse.builder()
                .error("ACCESS_DENIED")
                .errorDescription("Access denied for user")
                .errorInformation("Trace details 999")
                .build();

        BitrixApiException ex = new BitrixApiException(errorResponse, 403);
        assertThat(ex.getError()).isEqualTo("ACCESS_DENIED");
        assertThat(ex.getErrorDescription()).isEqualTo("Access denied for user");
        assertThat(ex.getErrorInformation()).isEqualTo("Trace details 999");
        assertThat(ex.getHttpStatusCode()).isEqualTo(403);
        assertThat(ex.getMessage()).isEqualTo("HTTP 403 - [ACCESS_DENIED] Access denied for user");

        // Null error response should gracefully default to UNKNOWN_ERROR
        BitrixApiException nullEx = new BitrixApiException(null, 500);
        assertThat(nullEx.getError()).isEqualTo("UNKNOWN_ERROR");
        assertThat(nullEx.getErrorDescription()).isNull();
        assertThat(nullEx.getErrorInformation()).isNull();
        assertThat(nullEx.getHttpStatusCode()).isEqualTo(500);
        assertThat(nullEx.getMessage()).isEqualTo("HTTP 500 - [UNKNOWN_ERROR]");
    }
}
