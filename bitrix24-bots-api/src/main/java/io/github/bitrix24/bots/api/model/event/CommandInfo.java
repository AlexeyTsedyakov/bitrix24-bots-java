package io.github.bitrix24.bots.api.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Command details provided in Bitrix24 ONIMBOTV2COMMANDADD events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommandInfo {

    private Long id;
    private String command;
    private String params;
    private String context;
}
