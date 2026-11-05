package io.github.bitrix24.bots.api.model.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
    private Object attach;
    private Object keyboard;
    private Boolean system;
    private Boolean urlPreview;

    public static CommandAnswerFields of(String message) {
        return CommandAnswerFields.builder().message(message).build();
    }
}
