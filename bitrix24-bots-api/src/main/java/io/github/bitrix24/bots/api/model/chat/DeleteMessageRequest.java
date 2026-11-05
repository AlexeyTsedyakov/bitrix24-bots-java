package io.github.bitrix24.bots.api.model.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * Request payload for {@code imbot.v2.Chat.Message.delete}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeleteMessageRequest {

    private Long botId;
    @ToString.Exclude
    private String botToken;
    private Long messageId;
    private Boolean complete;

    public static DeleteMessageRequest of(Long messageId) {
        return DeleteMessageRequest.builder().messageId(messageId).build();
    }

    public static DeleteMessageRequest of(Long messageId, boolean complete) {
        return DeleteMessageRequest.builder()
                .messageId(messageId)
                .complete(complete)
                .build();
    }
}
