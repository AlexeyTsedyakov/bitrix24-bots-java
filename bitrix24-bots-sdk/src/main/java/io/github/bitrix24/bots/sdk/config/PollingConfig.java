package io.github.bitrix24.bots.sdk.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Configuration options for bot polling mode ({@code imbot.v2.Event.get}).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PollingConfig {

    /**
     * Initial offset cursor for the polling queue.
     * If null, polling starts from the latest unprocessed events.
     */
    private Long initialOffset;

    /**
     * Maximum number of events to fetch per polling request (1–1000). Default is 100.
     */
    @Builder.Default
    private int limit = 100;

    /**
     * Whether to request user events ({@code ONIMV2*}) along with bot events. Default is false.
     */
    @Builder.Default
    private boolean withUserEvents = false;

    /**
     * Polling pause interval in milliseconds when no new events are returned. Default is 1000ms.
     */
    @Builder.Default
    private long intervalMs = 1000L;

    /**
     * Minimum delay in milliseconds for exponential backoff on network errors. Default is 1000ms.
     */
    @Builder.Default
    private long minBackoffMs = 1000L;

    /**
     * Maximum delay in milliseconds for exponential backoff on network errors. Default is 30000ms.
     */
    @Builder.Default
    private long maxBackoffMs = 30000L;

    /**
     * Backoff multiplier applied upon consecutive network or server failures. Default is 2.0.
     */
    @Builder.Default
    private double backoffMultiplier = 2.0;

    /**
     * Explicit bot ID override. If null, taken from {@link BotConfig#getBotId()}.
     */
    private Long botId;

    /**
     * Explicit bot token override. If null, taken from {@link BotConfig#getBotToken()}.
     */
    @ToString.Exclude
    private String botToken;

    /**
     * Whether the background polling thread should run as a daemon thread.
     * Defaults to {@code false} so standalone applications stay alive while polling is active.
     */
    @Builder.Default
    private boolean daemon = false;
}
