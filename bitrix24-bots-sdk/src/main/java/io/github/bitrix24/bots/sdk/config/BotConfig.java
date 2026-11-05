package io.github.bitrix24.bots.sdk.config;

import io.github.bitrix24.bots.api.model.event.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Configuration for Bitrix24 Bot event receiving, authentication, and event filtering.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotConfig {

    /**
     * Set of event types permitted for dispatching.
     * By default, all {@link EventType} events are enabled.
     */
    @Builder.Default
    private Set<EventType> allowedEvents = EnumSet.allOf(EventType.class);

    /**
     * Secret application token assigned by Bitrix24 to verify incoming webhook notifications.
     */
    @ToString.Exclude
    private String applicationToken;

    /**
     * Bot authorization token (used in REST API calls for webhook mode).
     */
    @ToString.Exclude
    private String botToken;

    /**
     * Bot identifier in Bitrix24.
     */
    private Long botId;

    /**
     * Unique bot code in Bitrix24.
     */
    private String botCode;

    /**
     * Returns an unmodifiable set of allowed event types.
     * Always returns a non-empty set (defaults to all types if unset).
     *
     * @return set of allowed event types
     */
    public Set<EventType> getAllowedEvents() {
        if (allowedEvents == null || allowedEvents.isEmpty()) {
            return Collections.unmodifiableSet(EnumSet.allOf(EventType.class));
        }
        return Collections.unmodifiableSet(allowedEvents);
    }

    /**
     * Checks if a given event type is allowed.
     *
     * @param type event type to test
     * @return true if allowed, false otherwise
     */
    public boolean isAllowed(EventType type) {
        if (type == null) {
            return false;
        }
        return getAllowedEvents().contains(type);
    }
}
