package io.github.bitrix24.bots.api.model.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import tools.jackson.databind.JsonNode;

import java.time.Instant;

/**
 * Message details provided in Bitrix24 bot events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageInfo {

    private Long id;

    @JsonAlias({"chat_id", "chatId"})
    private Long chatId;

    @JsonAlias({"author_id", "authorId"})
    private Long authorId;

    private Instant date;

    private String text;

    @JsonAlias({"is_system", "isSystem", "system"})
    private Boolean isSystem;

    private String uuid;

    private JsonNode forward;

    private JsonNode params;

    @JsonAlias({"viewed_by_others", "viewedByOthers"})
    private Boolean viewedByOthers;

    @JsonAlias({"reply_id", "replyId"})
    private Long replyId;
}
