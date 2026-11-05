package io.github.bitrix24.bots.api.model.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * Request payload for sending typing/input action notification ({@code imbot.v2.Chat.InputAction.notify}).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendTypingRequest {

    private Long botId;
    @ToString.Exclude
    private String botToken;
    private String dialogId;
    private String statusMessageCode;
    private Integer duration;

    public static SendTypingRequest of(String dialogId) {
        return SendTypingRequest.builder().dialogId(dialogId).build();
    }

    public static SendTypingRequest of(String dialogId, int durationSeconds) {
        return SendTypingRequest.builder()
                .dialogId(dialogId)
                .duration(durationSeconds)
                .build();
    }
}
