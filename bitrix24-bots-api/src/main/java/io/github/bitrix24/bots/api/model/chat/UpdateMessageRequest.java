package io.github.bitrix24.bots.api.model.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * Request payload for {@code imbot.v2.Chat.Message.update}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateMessageRequest {

    private Long botId;
    @ToString.Exclude
    private String botToken;
    private Long messageId;
    private UpdateMessageFields fields;

    public static UpdateMessageRequest of(Long messageId, String newMessage) {
        return UpdateMessageRequest.builder()
                .messageId(messageId)
                .fields(UpdateMessageFields.of(newMessage))
                .build();
    }

    public static UpdateMessageRequest of(Long messageId, UpdateMessageFields fields) {
        return UpdateMessageRequest.builder()
                .messageId(messageId)
                .fields(fields)
                .build();
    }
}
