package io.github.bitrix24.bots.api.model.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.OffsetDateTime;

/**
 * Execution timing metadata returned in Bitrix24 REST API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BitrixTime {

    /**
     * Unix timestamp with fractional seconds of request start.
     */
    private Double start;

    /**
     * Unix timestamp with fractional seconds of request finish.
     */
    private Double finish;

    /**
     * Total request duration in seconds.
     */
    private Double duration;

    /**
     * Internal server processing time in seconds.
     */
    private Double processing;

    /**
     * ISO-8601 date and time when request execution started.
     */
    @JsonProperty("date_start")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime dateStart;

    /**
     * ISO-8601 date and time when request execution finished.
     */
    @JsonProperty("date_finish")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime dateFinish;

    /**
     * Operation time spent in seconds (from the portal's rate limit budget).
     */
    private Double operating;

    /**
     * Unix timestamp in seconds when the operation time limit resets.
     */
    @JsonProperty("operating_reset_at")
    private Long operatingResetAt;

    /**
     * Helper to get request start as {@link Instant}.
     *
     * @return Instant or null
     */
    public Instant getStartInstant() {
        return dateStart != null ? dateStart.toInstant() : null;
    }

    /**
     * Helper to get request finish as {@link Instant}.
     *
     * @return Instant or null
     */
    public Instant getFinishInstant() {
        return dateFinish != null ? dateFinish.toInstant() : null;
    }
}
