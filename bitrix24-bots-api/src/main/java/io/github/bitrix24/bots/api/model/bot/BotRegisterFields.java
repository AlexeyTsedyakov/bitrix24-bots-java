package io.github.bitrix24.bots.api.model.bot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * Fields used in {@link BotRegisterRequest} for {@code imbot.v2.Bot.register}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class BotRegisterFields {

    private String code;
    @ToString.Exclude
    private String botToken;
    private BotProperties properties;
    private String type;
    private String eventMode;
    private String webhookUrl;
    private Boolean isHidden;
    private Boolean isReactionsEnabled;
    private Boolean isSupportOpenline;
    private String backgroundId;
}
