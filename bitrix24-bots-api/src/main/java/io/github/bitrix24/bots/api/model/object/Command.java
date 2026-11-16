package io.github.bitrix24.bots.api.model.object;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Slash command details provided in {@code ONIMBOTV2COMMANDADD} events and {@code imbot.v2.Command.*} responses.
 * <p>
 * The {@code id} field is represented as {@link String} because {@code imbot.v2.Command.list} returns string
 * identifiers (such as {@code "def0"}) for built-in Bitrix24 commands alongside numeric IDs for bot commands.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class Command {

    private String id;
    private Long botId;
    private String command;
    private Boolean common;
    private Boolean hidden;
    private Boolean extranetSupport;

    // Additional fields (ONIMBOTV2COMMANDADD event and imbot.v2.Command.list)
    private String params;
    private String context;

    // Additional fields (only in imbot.v2.Command.list)
    private String category;
    private String title;
    private Boolean extranet;
}
