package io.github.bitrix24.bots.api.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BitrixApiConfigTest {

    @Test
    @DisplayName("Webhook config normalization adds trailing slash")
    void testWebhookNormalization() {
        BitrixApiConfig configWithoutSlash = BitrixApiConfig.ofWebhook("https://example.bitrix24.ru/rest/1/secret_token");
        assertThat(configWithoutSlash.getNormalizedBaseUrl())
                .isEqualTo("https://example.bitrix24.ru/rest/1/secret_token/");

        BitrixApiConfig configWithSlash = BitrixApiConfig.ofWebhook("https://example.bitrix24.ru/rest/1/secret_token/");
        assertThat(configWithSlash.getNormalizedBaseUrl())
                .isEqualTo("https://example.bitrix24.ru/rest/1/secret_token/");

        assertThat(configWithSlash.isWebhook()).isTrue();
        assertThat(configWithSlash.isOAuth()).isFalse();
    }

    @Test
    @DisplayName("getMethodUrl constructs full endpoint URL")
    void testGetMethodUrl() {
        BitrixApiConfig config = BitrixApiConfig.ofWebhook("https://example.bitrix24.ru/rest/1/secret/");

        assertThat(config.getMethodUrl("imbot.v2.Bot.register"))
                .isEqualTo("https://example.bitrix24.ru/rest/1/secret/imbot.v2.Bot.register");

        // Leading slash in method name should be safely stripped
        assertThat(config.getMethodUrl("/imbot.v2.Event.get"))
                .isEqualTo("https://example.bitrix24.ru/rest/1/secret/imbot.v2.Event.get");
    }

    @Test
    @DisplayName("getMethodUrl throws exception when baseUrl is not configured")
    void testGetMethodUrlWithoutBaseUrl() {
        BitrixApiConfig emptyConfig = BitrixApiConfig.builder().build();
        assertThatThrownBy(() -> emptyConfig.getMethodUrl("imbot.v2.Bot.register"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("baseUrl is not configured");
    }

    @Test
    @DisplayName("OAuth configuration flags")
    void testOAuthConfig() {
        BitrixApiConfig oauthConfig = BitrixApiConfig.ofOAuth("https://example.bitrix24.ru/rest/", "access_token_123");

        assertThat(oauthConfig.isOAuth()).isTrue();
        assertThat(oauthConfig.isWebhook()).isFalse();
        assertThat(oauthConfig.getOauthToken()).isEqualTo("access_token_123");
        assertThat(oauthConfig.getMethodUrl("imbot.v2.Bot.list"))
                .isEqualTo("https://example.bitrix24.ru/rest/imbot.v2.Bot.list");
    }

    @Test
    @DisplayName("getMethodUrl validates method names against malformed characters")
    void testGetMethodUrlValidation() {
        BitrixApiConfig config = BitrixApiConfig.ofWebhook("https://example.bitrix24.ru/rest/1/secret/");

        // Null method
        assertThatThrownBy(() -> config.getMethodUrl(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("REST method name must not be null");

        // Blank or whitespace-only
        assertThatThrownBy(() -> config.getMethodUrl(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("REST method name must not be blank");
        assertThatThrownBy(() -> config.getMethodUrl("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("REST method name must not be blank");

        // Lone slash
        assertThatThrownBy(() -> config.getMethodUrl("/"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid REST method name");

        // Dot-only names are normalized as path segments by URL clients
        assertThatThrownBy(() -> config.getMethodUrl("."))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid REST method name");
        assertThatThrownBy(() -> config.getMethodUrl(".."))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid REST method name");

        // Method containing query params, fragments, directory traversal, or slashes
        assertThatThrownBy(() -> config.getMethodUrl("imbot.v2.Bot.register?admin=1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid REST method name");
        assertThatThrownBy(() -> config.getMethodUrl("imbot.v2.Bot.register#section"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid REST method name");
        assertThatThrownBy(() -> config.getMethodUrl("../admin.php"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid REST method name");
        assertThatThrownBy(() -> config.getMethodUrl("nested/slash/method"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid REST method name");
    }

    @Test
    @DisplayName("Bot credentials builder and toString masking protects secret webhook key")
    void testBotCredentials() {
        BitrixApiConfig config = BitrixApiConfig.builder()
                .baseUrl("https://example.bitrix24.ru/rest/1/webhook_secret_key_12345/")
                .botId(456L)
                .botToken("secret_bot_token")
                .applicationToken("app_token_xyz")
                .oauthToken("secret_oauth_token")
                .build();

        assertThat(config.getBotId()).isEqualTo(456L);
        assertThat(config.getBotToken()).isEqualTo("secret_bot_token");
        assertThat(config.getApplicationToken()).isEqualTo("app_token_xyz");
        assertThat(config.getOauthToken()).isEqualTo("secret_oauth_token");

        // getMaskedBaseUrl masks the webhook secret
        assertThat(config.getMaskedBaseUrl())
                .isEqualTo("https://example.bitrix24.ru/rest/1/***/");

        // Sensitive tokens must never be exposed in toString()
        String stringRep = config.toString();
        assertThat(stringRep).doesNotContain("webhook_secret_key_12345");
        assertThat(stringRep).doesNotContain("secret_bot_token");
        assertThat(stringRep).doesNotContain("app_token_xyz");
        assertThat(stringRep).doesNotContain("secret_oauth_token");
        assertThat(stringRep).contains("https://example.bitrix24.ru/rest/1/***/");
    }
}
