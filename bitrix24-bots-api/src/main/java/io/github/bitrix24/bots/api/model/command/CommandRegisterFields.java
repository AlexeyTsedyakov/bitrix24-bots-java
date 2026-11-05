package io.github.bitrix24.bots.api.model.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

/**
 * Fields used in {@link CommandRegisterRequest} for {@code imbot.v2.Command.register}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommandRegisterFields {

    private String command;
    private Map<String, String> title;
    private Map<String, String> params;
    private Boolean common;
    private Boolean hidden;
    private Boolean extranetSupport;

    public static CommandRegisterFields of(String command, String defaultTitle) {
        return CommandRegisterFields.builder()
                .command(command)
                .title(Map.of("en", defaultTitle, "ru", defaultTitle))
                .build();
    }
}
