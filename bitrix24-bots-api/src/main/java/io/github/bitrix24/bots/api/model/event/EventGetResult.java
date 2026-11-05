package io.github.bitrix24.bots.api.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.Collections;
import java.util.List;

/**
 * Result payload returned by {@code imbot.v2.Event.get}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventGetResult {

    @Builder.Default
    private List<Event> events = Collections.emptyList();
    private Long nextOffset;
    private Boolean hasMore;
}
