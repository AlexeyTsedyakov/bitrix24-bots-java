package io.github.bitrix24.bots.api.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * Request payload for {@code imbot.v2.Event.get}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventGetRequest {

    private Long botId;
    @ToString.Exclude
    private String botToken;
    private Long offset;
    private Integer limit;
    private Boolean withUserEvents;

    public static EventGetRequest ofOffset(Long offset) {
        return EventGetRequest.builder().offset(offset).build();
    }
}
