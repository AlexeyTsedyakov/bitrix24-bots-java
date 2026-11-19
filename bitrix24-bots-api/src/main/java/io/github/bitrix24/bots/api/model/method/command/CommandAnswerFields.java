package io.github.bitrix24.bots.api.model.method.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.bitrix24.bots.api.json.KeyboardEmptyFilter;
import io.github.bitrix24.bots.api.model.method.message.attach.Attach;
import io.github.bitrix24.bots.api.model.method.message.keyboard.Keyboard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Fields payload for {@code imbot.v2.Command.answer}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommandAnswerFields {

    private String message;
    private Attach attach;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = KeyboardEmptyFilter.class)
    private Keyboard keyboard;

    private Boolean system;
    private Boolean urlPreview;

    public static CommandAnswerFields of(String message) {
        return CommandAnswerFields.builder()
                .message(message)
                .build();
    }

    public static CommandAnswerFields of(String message, Keyboard keyboard) {
        return CommandAnswerFields.builder()
                .message(message)
                .keyboard(keyboard)
                .build();
    }
}
