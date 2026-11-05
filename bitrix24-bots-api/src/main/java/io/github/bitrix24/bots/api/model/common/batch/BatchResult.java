package io.github.bitrix24.bots.api.model.common.batch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.bitrix24.bots.api.model.common.BitrixTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.Collections;
import java.util.Map;

/**
 * Result payload returned by Bitrix24 batch method.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchResult {

    @Builder.Default
    private Map<String, Object> result = Collections.emptyMap();

    @JsonProperty("result_error")
    @Builder.Default
    private Map<String, Object> resultError = Collections.emptyMap();

    @JsonProperty("result_total")
    @Builder.Default
    private Map<String, Integer> resultTotal = Collections.emptyMap();

    @JsonProperty("result_next")
    @Builder.Default
    private Map<String, Integer> resultNext = Collections.emptyMap();

    @JsonProperty("result_time")
    @Builder.Default
    private Map<String, BitrixTime> resultTime = Collections.emptyMap();
}
