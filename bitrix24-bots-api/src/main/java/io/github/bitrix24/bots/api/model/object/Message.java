package io.github.bitrix24.bots.api.model.object;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import tools.jackson.databind.JsonNode;

import java.time.Instant;

/**
 * Message details provided in Bitrix24 bot events and {@code imbot.v2} / {@code im.v2} responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {

    private Long id;
    private Long chatId;
    private Long authorId;
    private Instant date;
    private String text;
    private Boolean isSystem;
    private String uuid;
    private JsonNode forward;
    private JsonNode params;
    private Boolean viewedByOthers;

    // Additional fields (only in method responses, e.g., imbot.v2.Chat.Message.get)
    private Boolean unread;
    private Boolean viewed;
}
