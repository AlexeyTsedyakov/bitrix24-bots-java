package io.github.bitrix24.bots.api.model.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Bot details provided in Bitrix24 bot events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class BotInfo {

    private Long id;
    private String code;
    private String type;

    @JsonAlias({"is_hidden", "isHidden"})
    private Boolean isHidden;

    @JsonAlias({"is_support_openline", "isSupportOpenline"})
    private Boolean isSupportOpenline;

    @JsonAlias({"is_reactions_enabled", "isReactionsEnabled"})
    private Boolean isReactionsEnabled;

    @JsonAlias({"background_id", "backgroundId"})
    private String backgroundId;

    private String language;

    @JsonAlias({"module_id", "moduleId"})
    private String moduleId;

    @JsonAlias({"event_mode", "eventMode"})
    private String eventMode;

    @JsonAlias({"count_message", "countMessage"})
    private Integer countMessage;

    @JsonAlias({"count_command", "countCommand"})
    private Integer countCommand;

    @JsonAlias({"count_chat", "countChat"})
    private Integer countChat;

    @JsonAlias({"count_user", "countUser"})
    private Integer countUser;

    private AuthInfo auth;
}
