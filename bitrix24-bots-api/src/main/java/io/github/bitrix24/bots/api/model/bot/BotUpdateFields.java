package io.github.bitrix24.bots.api.model.bot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * Fields used in {@link BotUpdateRequest} for {@code imbot.v2.Bot.update}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class BotUpdateFields {

    private BotProperties properties;
    private Boolean isHidden;
    private Boolean isReactionsEnabled;
    private Boolean isSupportOpenline;
    private String backgroundId;
    private String eventMode;
    private String webhookUrl;
    @ToString.Exclude
    private String botToken;
}
