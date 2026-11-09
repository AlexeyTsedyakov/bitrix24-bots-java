package io.github.bitrix24.bots.api.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;

/**
 * Chat details provided in Bitrix24 bot events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatInfo {

    private Long id;
    private String dialogId;
    private String name;
    private String type;
    private String messageType;
    private Long owner;
    private String color;
    private String avatar;
    private String description;
    private Boolean extranet;
    private String role;
    private Boolean containsCollaber;
    private List<Long> muteList;
    private String entityType;
    private String entityId;
    private String entityData1;
    private String entityData2;
    private String entityData3;
    private JsonNode entityLink;
    private Long diskFolderId;
    private JsonNode permissions;
    private Long parentChatId;
    private Long parentMessageId;
    private Boolean isNew;
    private String textFieldEnabled;
    private String backgroundId;

    // Additional fields (only in method responses, e.g., imbot.v2.Chat.get)
    private Instant dateCreate;
    private Long lastMessageId;
    private Long lastId;
    private List<Long> managerList;
    private Integer messageCount;
    private Integer userCounter;
    private Long unreadId;
    private JsonNode lastMessageViews;
    private Long markedId;

    @JsonProperty("public")
    private JsonNode publicSettings;
}
