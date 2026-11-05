package io.github.bitrix24.bots.sdk.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Configuration options for bot webhook receiver and embedded HTTP server.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookConfig {

    /**
     * Port to bind the embedded webhook HTTP server. Default is 8080.
     * Set to 0 to allocate an ephemeral free port (useful for tests).
     */
    @Builder.Default
    private int port = 8080;

    /**
     * Host address to bind the embedded webhook HTTP server. Default is "0.0.0.0".
     */
    @Builder.Default
    private String host = "0.0.0.0";

    /**
     * URL path where webhook requests are received. Default is "/bitrix24/webhook".
     */
    @Builder.Default
    private String path = "/bitrix24/webhook";

    /**
     * Maximum number of incoming TCP connections to queue. Default is 50.
     */
    @Builder.Default
    private int backlog = 50;

    /**
     * Secret application token to verify incoming Bitrix24 webhook notifications.
     * If specified, overrides {@link BotConfig#getApplicationToken()}.
     */
    @ToString.Exclude
    private String applicationToken;
}
