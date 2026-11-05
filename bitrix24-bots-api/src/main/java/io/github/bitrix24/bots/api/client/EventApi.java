package io.github.bitrix24.bots.api.client;

import io.github.bitrix24.bots.api.model.common.BitrixResponse;
import io.github.bitrix24.bots.api.model.event.EventGetRequest;
import io.github.bitrix24.bots.api.model.event.EventGetResult;

/**
 * Client interface for polling event queue via {@code imbot.v2.Event.get}.
 */
public interface EventApi {

    /**
     * Retrieves pending events from bot queue via {@code imbot.v2.Event.get}.
     */
    BitrixResponse<EventGetResult> get(EventGetRequest request);

    default BitrixResponse<EventGetResult> get() {
        return get(EventGetRequest.builder().build());
    }

    default BitrixResponse<EventGetResult> get(Long offset) {
        return get(EventGetRequest.ofOffset(offset));
    }

    default BitrixResponse<EventGetResult> get(Long offset, int limit) {
        return get(EventGetRequest.builder().offset(offset).limit(limit).build());
    }

    default BitrixResponse<EventGetResult> get(Long offset, int limit, boolean withUserEvents) {
        return get(EventGetRequest.builder()
                .offset(offset)
                .limit(limit)
                .withUserEvents(withUserEvents)
                .build());
    }
}
