package io.github.bitrix24.bots.api.model.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * Request payload for {@code imbot.v2.Command.register}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommandRegisterRequest {

    private Long botId;
    @ToString.Exclude
    private String botToken;
    private CommandRegisterFields fields;

    public static CommandRegisterRequest of(CommandRegisterFields fields) {
        return CommandRegisterRequest.builder().fields(fields).build();
    }
}
