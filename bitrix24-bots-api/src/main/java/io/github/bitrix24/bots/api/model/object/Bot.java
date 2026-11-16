package io.github.bitrix24.bots.api.model.object;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.bitrix24.bots.api.model.event.Auth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Bot details provided in Bitrix24 bot events and {@code imbot.v2.Bot.*} responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class Bot {

    private Long id;
    private String code;
    private String type;
    private Boolean isHidden;
    private Boolean isSupportOpenline;
    private Boolean isReactionsEnabled;
    private String backgroundId;
    private String language;

    // Additional fields (only for bot owner)
    private String moduleId;
    private String eventMode;
    private Integer countMessage;
    private Integer countCommand;
    private Integer countChat;
    private Integer countUser;

    // Webhook-mode event field (data.bot.auth)
    private Auth auth;
}
