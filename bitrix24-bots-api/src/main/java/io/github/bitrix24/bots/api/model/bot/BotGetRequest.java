package io.github.bitrix24.bots.api.model.bot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * Request payload for {@code imbot.v2.Bot.get}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class BotGetRequest {

    private Long botId;
    private String code;
    @ToString.Exclude
    private String botToken;

    public static BotGetRequest ofId(Long botId) {
        return BotGetRequest.builder().botId(botId).build();
    }

    public static BotGetRequest ofCode(String code) {
        return BotGetRequest.builder().code(code).build();
    }
}
