package io.github.bitrix24.bots.api.model.bot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * Request payload for {@code imbot.v2.Bot.update}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class BotUpdateRequest {

    private Long botId;
    @ToString.Exclude
    private String botToken;
    private BotUpdateFields fields;

    public static BotUpdateRequest of(Long botId, BotUpdateFields fields) {
        return BotUpdateRequest.builder()
                .botId(botId)
                .fields(fields)
                .build();
    }
}
