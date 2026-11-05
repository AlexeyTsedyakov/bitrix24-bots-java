package io.github.bitrix24.bots.api.model.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * Request payload for {@code imbot.v2.Command.unregister}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommandUnregisterRequest {

    private Long botId;
    @ToString.Exclude
    private String botToken;
    private Long commandId;

    public static CommandUnregisterRequest of(Long commandId) {
        return CommandUnregisterRequest.builder().commandId(commandId).build();
    }
}
