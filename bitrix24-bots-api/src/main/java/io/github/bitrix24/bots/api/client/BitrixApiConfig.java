package io.github.bitrix24.bots.api.client;

import lombok.*;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Configuration for connecting to Bitrix24 REST API.
 * Supports authentication via inbound webhook URL or OAuth 2.0 access token,
 * as well as bot-specific credentials (botId, botToken, applicationToken).
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class BitrixApiConfig {

    private static final Pattern WEBHOOK_URL_PATTERN =
            Pattern.compile("^(https?://[^/]+/rest/\\d+/)([^/?#]+)(/?.*)$", Pattern.CASE_INSENSITIVE);

    private static final Pattern METHOD_NAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_.]+$");

    /**
     * Base REST API URL.
     * For webhook: {@code https://<portal>.bitrix24.ru/rest/<userId>/<webhookKey>/}
     * For OAuth: {@code https://<portal>.bitrix24.ru/rest/}
     */
    private String baseUrl;

    /**
     * Chat bot ID in Bitrix24.
     */
    private Long botId;

    /**
     * Secret bot token assigned during registration or required for imbot.v2 methods.
     */
    private String botToken;

    /**
     * Application token (auth.application_token) sent by Bitrix24 in events and webhooks
     * to verify the authenticity of requests.
     */
    private String applicationToken;

    /**
     * OAuth 2.0 access token (optional, when OAuth authorization is used instead of webhook).
     */
    private String oauthToken;

    /**
     * Creates a configuration from an inbound webhook URL.
     *
     * @param webhookUrl the full webhook URL
     * @return new BitrixApiConfig instance
     */
    public static BitrixApiConfig ofWebhook(String webhookUrl) {
        return BitrixApiConfig.builder()
                .baseUrl(webhookUrl)
                .build();
    }

    /**
     * Creates a configuration from an inbound webhook URL and bot credentials.
     *
     * @param webhookUrl the full webhook URL
     * @param botId the bot ID
     * @param botToken the bot authorization token
     * @return new BitrixApiConfig instance
     */
    public static BitrixApiConfig ofWebhook(String webhookUrl, Long botId, String botToken) {
        return BitrixApiConfig.builder()
                .baseUrl(webhookUrl)
                .botId(botId)
                .botToken(botToken)
                .build();
    }

    public static BitrixApiConfig fromWebhook(String webhookUrl) {
        return ofWebhook(webhookUrl);
    }

    public static BitrixApiConfig fromWebhook(String webhookUrl, Long botId, String botToken) {
        return ofWebhook(webhookUrl, botId, botToken);
    }

    /**
     * Creates a configuration for OAuth-based API access.
     *
     * @param portalRestUrl portal REST URL (e.g., {@code https://example.bitrix24.ru/rest/})
     * @param oauthToken OAuth 2.0 access token
     * @return new BitrixApiConfig instance
     */
    public static BitrixApiConfig ofOAuth(String portalRestUrl, String oauthToken) {
        return BitrixApiConfig.builder()
                .baseUrl(portalRestUrl)
                .oauthToken(oauthToken)
                .build();
    }

    public static BitrixApiConfig fromOAuth(String portalRestUrl, String oauthToken) {
        return ofOAuth(portalRestUrl, oauthToken);
    }

    /**
     * Returns the normalized base URL ending with a trailing slash.
     *
     * @return normalized base URL or null if baseUrl is not set
     */
    public String getNormalizedBaseUrl() {
        if (baseUrl == null) {
            return null;
        }
        String trimmed = baseUrl.trim();
        return trimmed.endsWith("/") ? trimmed : trimmed + "/";
    }

    /**
     * Returns the base URL with secret webhook tokens masked (e.g. {@code https://portal/rest/1/*** /}).
     *
     * @return safe base URL for logging and display
     */
    public String getMaskedBaseUrl() {
        if (baseUrl == null) {
            return null;
        }
        Matcher matcher = WEBHOOK_URL_PATTERN.matcher(baseUrl.trim());
        if (matcher.matches()) {
            return matcher.group(1) + "***" + (matcher.group(3).startsWith("/") ? "/" : "");
        }
        return baseUrl;
    }

    /**
     * Resolves the full URL for the given Bitrix24 REST method.
     *
     * @param method REST method name (e.g. {@code imbot.v2.Bot.register})
     * @return full endpoint URL
     */
    public String getMethodUrl(String method) {
        Objects.requireNonNull(method, "REST method name must not be null");
        String trimmed = method.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("REST method name must not be blank");
        }
        String cleanMethod = trimmed.startsWith("/") ? trimmed.substring(1) : trimmed;
        if (cleanMethod.isEmpty() || cleanMethod.equals(".") || cleanMethod.equals("..") ||
                !METHOD_NAME_PATTERN.matcher(cleanMethod).matches()) {
            throw new IllegalArgumentException("Invalid REST method name: '" + method +
                    "'. Method name must only contain alphanumeric characters, underscores, and dots.");
        }
        String base = getNormalizedBaseUrl();
        if (base == null || base.isEmpty()) {
            throw new IllegalStateException("baseUrl is not configured");
        }
        return base + cleanMethod;
    }

    /**
     * Checks whether OAuth token authentication is configured.
     *
     * @return true if oauthToken is present
     */
    public boolean isOAuth() {
        return oauthToken != null && !oauthToken.isBlank();
    }

    /**
     * Checks whether webhook authentication is configured (baseUrl contains /rest/<userId>/<webhookCode>/).
     *
     * @return true if webhook URL is configured
     */
    public boolean isWebhook() {
        if (baseUrl == null || baseUrl.isBlank() || isOAuth()) {
            return false;
        }
        return WEBHOOK_URL_PATTERN.matcher(baseUrl.trim()).matches();
    }

    @Override
    public String toString() {
        return "BitrixApiConfig(" +
                "baseUrl=" + getMaskedBaseUrl() +
                ", botId=" + botId +
                ", botToken=" + (botToken != null ? "***" : "null") +
                ", applicationToken=" + (applicationToken != null ? "***" : "null") +
                ", oauthToken=" + (oauthToken != null ? "***" : "null") +
                ")";
    }
}
