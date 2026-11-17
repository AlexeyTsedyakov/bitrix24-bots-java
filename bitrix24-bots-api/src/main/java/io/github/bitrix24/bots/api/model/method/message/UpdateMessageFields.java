package io.github.bitrix24.bots.api.model.method.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.bitrix24.bots.api.json.KeyboardUpdateSerializer;
import io.github.bitrix24.bots.api.model.method.message.attach.Attach;
import io.github.bitrix24.bots.api.model.method.message.keyboard.Keyboard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Fields payload for {@code imbot.v2.Chat.Message.update}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateMessageFields {

    private String message;
    private Attach attach;

    @JsonSerialize(using = KeyboardUpdateSerializer.class)
    private Keyboard keyboard;

    private String urlPreview;

    public static UpdateMessageFields of(String message) {
        return UpdateMessageFields.builder().message(message).build();
    }
}
