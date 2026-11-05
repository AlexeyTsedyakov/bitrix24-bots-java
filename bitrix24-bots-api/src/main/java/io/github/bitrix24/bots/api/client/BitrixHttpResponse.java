package io.github.bitrix24.bots.api.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Representation of an HTTP response returned by {@link BitrixHttpClient}.
 */
@Getter
@Builder
@ToString
@AllArgsConstructor
public class BitrixHttpResponse {

    private final int statusCode;
    private final String body;

    @Builder.Default
    private final Map<String, List<String>> headers = Collections.emptyMap();

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }
}
