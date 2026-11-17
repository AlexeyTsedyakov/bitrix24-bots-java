package io.github.bitrix24.bots.api.model.method.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

/**
 * Enumeration of Bitrix24 {@code imbot.v2} bot event types ({@code ONIMBOTV2*})
 * and {@code im.v2} user event types ({@code ONIMV2*}, returned when {@code withUserEvents} is enabled).
 */
public enum EventType {

    /**
     * User sent a message to the bot or mentioned the bot in a group chat.
     */
    ONIMBOTV2MESSAGEADD,

    /**
     * User invoked a slash command registered by the bot.
     */
    ONIMBOTV2COMMANDADD,

    /**
     * Bot was added to a chat or received an invitation.
     */
    ONIMBOTV2JOINCHAT,

    /**
     * Bot was deleted from the portal (final event).
     */
    ONIMBOTV2DELETE,

    /**
     * Message in a bot chat was updated/edited.
     */
    ONIMBOTV2MESSAGEUPDATE,

    /**
     * Message in a bot chat was deleted.
     */
    ONIMBOTV2MESSAGEDELETE,

    /**
     * Reaction to a message was added or removed.
     */
    ONIMBOTV2REACTIONCHANGE,

    /**
     * User opened dialogue with bot passing external context data.
     */
    ONIMBOTV2CONTEXTGET,

    /**
     * User event: new message in a chat where the subscribed user is a participant.
     */
    ONIMV2MESSAGEADD,

    /**
     * User event: message in a chat was updated/edited.
     */
    ONIMV2MESSAGEUPDATE,

    /**
     * User event: message in a chat was deleted.
     */
    ONIMV2MESSAGEDELETE,

    /**
     * User event: reaction to a message was added or removed.
     */
    ONIMV2REACTIONCHANGE,

    /**
     * User event: new participant was added to a chat.
     */
    ONIMV2JOINCHAT,

    /**
     * Unknown or unrecognized event type.
     */
    @JsonEnumDefaultValue
    UNKNOWN;

    /**
     * Safely parses an event name into an {@link EventType}.
     *
     * @param type event name string
     * @return matching EventType or {@link #UNKNOWN}
     */
    @JsonCreator
    public static EventType fromString(String type) {
        if (type == null || type.isBlank()) {
            return UNKNOWN;
        }
        String normalized = type.trim().toUpperCase();
        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
