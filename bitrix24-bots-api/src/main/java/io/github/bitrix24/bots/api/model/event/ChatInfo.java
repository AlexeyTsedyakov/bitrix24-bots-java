package io.github.bitrix24.bots.api.model.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

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

    /**
     * Chat ID (represented as String to support both numeric ID and string formats).
     */
    private String id;

    @JsonAlias({"dialog_id", "dialogId"})
    private String dialogId;

    private String type;
    private String name;

    @JsonAlias({"entity_type", "entityType"})
    private String entityType;

    private String owner;
    private String avatar;
    private String color;
    private String description;

    /**
     * Returns the numeric chat ID if parseable, or null.
     *
     * @return numeric ID or null
     */
    public Long getNumericId() {
        if (id == null) {
            return null;
        }
        try {
            if (id.startsWith("chat")) {
                return Long.parseLong(id.substring(4));
            }
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
