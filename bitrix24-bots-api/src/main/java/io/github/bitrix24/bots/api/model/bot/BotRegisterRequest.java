package io.github.bitrix24.bots.api.model.bot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Request payload for {@code imbot.v2.Bot.register}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class BotRegisterRequest {

    private BotRegisterFields fields;

    public static BotRegisterRequest of(BotRegisterFields fields) {
        return BotRegisterRequest.builder().fields(fields).build();
    }
}
