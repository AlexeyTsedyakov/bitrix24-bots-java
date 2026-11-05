package io.github.bitrix24.bots.api.model.chat.keyboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Representation of a single button in a Bitrix24 interactive keyboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyboardButton {

    @JsonProperty("TEXT")
    private String text;

    @JsonProperty("LINK")
    private String link;

    @JsonProperty("COMMAND")
    private String command;

    @JsonProperty("COMMAND_PARAMS")
    private String commandParams;

    @JsonProperty("ACTION")
    private String action;

    @JsonProperty("ACTION_VALUE")
    private String actionValue;

    @JsonProperty("BG_COLOR")
    private String bgColor;

    @JsonProperty("BG_COLOR_TOKEN")
    private String bgColorToken;

    @JsonProperty("TEXT_COLOR")
    private String textColor;

    @JsonProperty("DISPLAY")
    private String display;

    @JsonProperty("DISABLED")
    private String disabled;

    @JsonProperty("BLOCK")
    private String block;

    @JsonProperty("WAIT")
    private String wait;

    public static KeyboardButton link(String text, String url) {
        return KeyboardButton.builder()
                .text(text)
                .link(url)
                .build();
    }

    public static KeyboardButton command(String text, String command) {
        return KeyboardButton.builder()
                .text(text)
                .command(command)
                .build();
    }

    public static KeyboardButton command(String text, String command, String params) {
        return KeyboardButton.builder()
                .text(text)
                .command(command)
                .commandParams(params)
                .build();
    }

    public static KeyboardButton action(String text, String action, String actionValue) {
        return KeyboardButton.builder()
                .text(text)
                .action(action)
                .actionValue(actionValue)
                .build();
    }

    public static KeyboardButton put(String text, String textToPut) {
        return action(text, "PUT", textToPut);
    }

    public static KeyboardButton send(String text, String textToSend) {
        return action(text, "SEND", textToSend);
    }
}
