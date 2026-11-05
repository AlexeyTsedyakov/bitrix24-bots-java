package io.github.bitrix24.bots.api.model.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Result payload returned by {@code imbot.v2.Chat.Message.send}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendMessageResult {

    private Long id;
    private Object uuidMap;
    private Object failureMap;
    private Object legacyFailureMap;
}
