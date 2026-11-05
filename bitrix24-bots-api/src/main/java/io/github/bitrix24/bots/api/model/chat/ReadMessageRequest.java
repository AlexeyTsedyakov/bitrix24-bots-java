package io.github.bitrix24.bots.api.model.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * Request payload for {@code imbot.v2.Chat.Message.read}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadMessageRequest {

    private Long botId;
    @ToString.Exclude
    private String botToken;
    private String dialogId;
    private Long messageId;

    public static ReadMessageRequest of(String dialogId) {
        return ReadMessageRequest.builder().dialogId(dialogId).build();
    }

    public static ReadMessageRequest of(String dialogId, Long messageId) {
        return ReadMessageRequest.builder()
                .dialogId(dialogId)
                .messageId(messageId)
                .build();
    }
}
