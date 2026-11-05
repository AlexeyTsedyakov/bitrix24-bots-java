package io.github.bitrix24.bots.api.model.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * Request payload for {@code imbot.v2.Chat.Message.send}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendMessageRequest {

    private Long botId;
    @ToString.Exclude
    private String botToken;
    private String dialogId;
    private SendMessageFields fields;

    public static SendMessageRequest of(String dialogId, String message) {
        return SendMessageRequest.builder()
                .dialogId(dialogId)
                .fields(SendMessageFields.of(message))
                .build();
    }

    public static SendMessageRequest of(String dialogId, SendMessageFields fields) {
        return SendMessageRequest.builder()
                .dialogId(dialogId)
                .fields(fields)
                .build();
    }
}
