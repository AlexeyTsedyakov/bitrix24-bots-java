package io.github.bitrix24.bots.api.model.method.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.bitrix24.bots.api.model.object.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import tools.jackson.databind.JsonNode;

import java.util.List;

/**
 * Unified data payload structure for Bitrix24 {@code imbot.v2} and {@code im.v2} events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventData {

    private Bot bot;
    private Message message;
    private Chat chat;
    private User user;
    private Command command;
    private String dialogId;
    private Long messageId;
    private String reaction;
    private String action;
    private JsonNode context;
    private String language;
    private List<Message> additionalMessages;

    // Top-level webhook auth attached by BotEventDispatcher
    private Auth auth;
}
