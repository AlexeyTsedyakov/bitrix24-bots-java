package io.github.bitrix24.bots.api.model.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * Request payload for {@code imbot.v2.Command.answer}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommandAnswerRequest {

    private Long botId;
    @ToString.Exclude
    private String botToken;
    private Long commandId;
    private Long messageId;
    private String dialogId;
    private CommandAnswerFields fields;

    public static CommandAnswerRequest of(Long commandId, Long messageId, String dialogId, String answerMessage) {
        return CommandAnswerRequest.builder()
                .commandId(commandId)
                .messageId(messageId)
                .dialogId(dialogId)
                .fields(CommandAnswerFields.of(answerMessage))
                .build();
    }
}
