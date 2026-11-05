package io.github.bitrix24.bots.api.model.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Representation of a registered slash command in imbot.v2.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class Command {

    private Long id;
    private Long botId;
    private String command;
    private Boolean common;
    private Boolean hidden;
    private Boolean extranetSupport;
}
