package io.github.bitrix24.bots.api.model.common.batch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.HashMap;
import java.util.Map;

/**
 * Request payload for Bitrix24 batch method.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchRequest {

    @Builder.Default
    private Boolean halt = false;

    @Builder.Default
    private Map<String, Object> cmd = new HashMap<>();

    public BatchRequest addCommand(String name, String query) {
        if (cmd == null) {
            cmd = new HashMap<>();
        }
        cmd.put(name, query);
        return this;
    }

    public static BatchRequest of(Map<String, Object> cmd) {
        return BatchRequest.builder().cmd(cmd).build();
    }
}
