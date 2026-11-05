package io.github.bitrix24.bots.api.model.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import tools.jackson.databind.JsonNode;

/**
 * Unified data payload structure for Bitrix24 bot events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventData {

    private BotInfo bot;
    private MessageInfo message;
    private ChatInfo chat;
    private UserInfo user;
    private CommandInfo command;
    private AuthInfo auth;

    @JsonAlias({"dialog_id", "dialogId"})
    private String dialogId;

    @JsonAlias({"message_id", "messageId"})
    private Long messageId;

    private String reaction;
    private String action;
    private JsonNode context;
    private String language;
    private JsonNode params;
}
