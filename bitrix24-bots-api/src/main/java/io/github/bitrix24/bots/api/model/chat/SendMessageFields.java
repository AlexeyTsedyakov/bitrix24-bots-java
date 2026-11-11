package io.github.bitrix24.bots.api.model.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.bitrix24.bots.api.json.KeyboardEmptyFilter;
import io.github.bitrix24.bots.api.model.chat.attach.Attach;
import io.github.bitrix24.bots.api.model.chat.keyboard.Keyboard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Fields payload for {@code imbot.v2.Chat.Message.send}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendMessageFields {

    private String message;
    private Attach attach;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = KeyboardEmptyFilter.class)
    private Keyboard keyboard;

    private Boolean system;
    private Boolean urlPreview;
    private Long replyId;
    private String templateId;
    private Object forwardIds;

    public static SendMessageFields of(String message) {
        return SendMessageFields.builder().message(message).build();
    }
}
