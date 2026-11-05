package io.github.bitrix24.bots.api.model.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

/**
 * Unified representation of an incoming Bitrix24 bot event (both Polling {@code imbot.v2.Event.get} and Webhook).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {

    private Long eventId;

    @JsonAlias({"type", "event"})
    private EventType type;

    @JsonAlias({"date", "ts"})
    private Instant date;

    private EventData data;

    /**
     * Checks if this event is an incoming message ({@link EventType#ONIMBOTV2MESSAGEADD}).
     *
     * @return true if event is a message
     */
    public boolean isMessage() {
        return type == EventType.ONIMBOTV2MESSAGEADD;
    }

    /**
     * Checks if this event is a slash command invocation ({@link EventType#ONIMBOTV2COMMANDADD}).
     *
     * @return true if event is a command
     */
    public boolean isCommand() {
        return type == EventType.ONIMBOTV2COMMANDADD;
    }

    /**
     * Checks if this event represents adding the bot to a chat ({@link EventType#ONIMBOTV2JOINCHAT}).
     *
     * @return true if event is join chat
     */
    public boolean isJoinChat() {
        return type == EventType.ONIMBOTV2JOINCHAT;
    }

    /**
     * Checks if this event represents bot unregistration/deletion ({@link EventType#ONIMBOTV2DELETE}).
     *
     * @return true if event is bot delete
     */
    public boolean isDelete() {
        return type == EventType.ONIMBOTV2DELETE;
    }

    /**
     * Checks if this event represents an edited message ({@link EventType#ONIMBOTV2MESSAGEUPDATE}).
     *
     * @return true if event is message update
     */
    public boolean isMessageUpdate() {
        return type == EventType.ONIMBOTV2MESSAGEUPDATE;
    }

    /**
     * Checks if this event represents a deleted message ({@link EventType#ONIMBOTV2MESSAGEDELETE}).
     *
     * @return true if event is message delete
     */
    public boolean isMessageDelete() {
        return type == EventType.ONIMBOTV2MESSAGEDELETE;
    }

    /**
     * Checks if this event represents a message reaction change ({@link EventType#ONIMBOTV2REACTIONCHANGE}).
     *
     * @return true if event is reaction change
     */
    public boolean isReactionChange() {
        return type == EventType.ONIMBOTV2REACTIONCHANGE;
    }

    /**
     * Checks if this event represents passing context when opening a dialog ({@link EventType#ONIMBOTV2CONTEXTGET}).
     *
     * @return true if event is context get
     */
    public boolean isContextGet() {
        return type == EventType.ONIMBOTV2CONTEXTGET;
    }

    /**
     * Returns the chat identifier as string, or null if unavailable.
     *
     * @return chat ID or null
     */
    public String getChatId() {
        return data != null && data.getChat() != null ? data.getChat().getId() : null;
    }

    /**
     * Returns dialog identifier (e.g. "chat5" or "1"), checking both chat info and top-level data.
     *
     * @return dialog ID or null
     */
    public String getDialogId() {
        if (data == null) {
            return null;
        }
        if (data.getChat() != null && data.getChat().getDialogId() != null) {
            return data.getChat().getDialogId();
        }
        return data.getDialogId();
    }

    /**
     * Returns the incoming message text, or null if not a message event.
     *
     * @return message text or null
     */
    public String getMessageText() {
        return data != null && data.getMessage() != null ? data.getMessage().getText() : null;
    }

    /**
     * Returns the author/user ID, or null if not available.
     *
     * @return user ID or null
     */
    public Long getUserId() {
        return data != null && data.getUser() != null ? data.getUser().getId() : null;
    }

    /**
     * Returns the author/user display name, or null if not available.
     *
     * @return user name or null
     */
    public String getUserName() {
        return data != null && data.getUser() != null ? data.getUser().getName() : null;
    }

    /**
     * Returns the invoked command name (e.g. "/help"), or null if not a command event.
     *
     * @return command name or null
     */
    public String getCommandName() {
        return data != null && data.getCommand() != null ? data.getCommand().getCommand() : null;
    }

    /**
     * Returns parameters passed to the slash command, or null if not available.
     *
     * @return command parameters or null
     */
    public String getCommandParams() {
        return data != null && data.getCommand() != null ? data.getCommand().getParams() : null;
    }

    /**
     * Returns the bot ID for which this event is targeted, or null if not available.
     *
     * @return bot ID or null
     */
    public Long getBotId() {
        return data != null && data.getBot() != null ? data.getBot().getId() : null;
    }

    /**
     * Returns authentication information associated with this event.
     *
     * @return AuthInfo or null
     */
    public AuthInfo getAuth() {
        if (data == null) {
            return null;
        }
        if (data.getAuth() != null) {
            return data.getAuth();
        }
        return data.getBot() != null ? data.getBot().getAuth() : null;
    }
}
