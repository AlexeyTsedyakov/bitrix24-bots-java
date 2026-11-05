package io.github.bitrix24.bots.api.model.bot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

/**
 * Request payload for {@code imbot.v2.Bot.list}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class BotListRequest {

    @ToString.Exclude
    private String botToken;
    private Map<String, Object> filter;
    private Integer limit;
    private Integer offset;

    public static BotListRequest ofFilter(Map<String, Object> filter) {
        return BotListRequest.builder().filter(filter).build();
    }
}
