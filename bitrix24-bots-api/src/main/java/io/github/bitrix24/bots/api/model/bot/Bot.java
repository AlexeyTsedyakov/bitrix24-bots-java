package io.github.bitrix24.bots.api.model.bot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Representation of a Bitrix24 Chat Bot in imbot.v2.
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
    private String moduleId;
    private String eventMode;
    private Integer countMessage;
    private Integer countCommand;
    private Integer countChat;
    private Integer countUser;
}
