package io.github.bitrix24.bots.api.model.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

/**
 * Enumeration of Bitrix24 imbot.v2 bot event types.
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
