package io.github.bitrix24.bots.api.model.chat.keyboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Representation of an interactive Keyboard containing buttons.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Keyboard {

    @Builder.Default
    private List<KeyboardButton> buttons = new ArrayList<>();

    @JsonValue
    public List<KeyboardButton> getButtons() {
        return buttons;
    }

    public Keyboard addButton(KeyboardButton button) {
        if (buttons == null) {
            buttons = new ArrayList<>();
        }
        buttons.add(button);
        return this;
    }

    public static Keyboard of(KeyboardButton... buttons) {
        return Keyboard.builder()
                .buttons(new ArrayList<>(Arrays.asList(buttons)))
                .build();
    }

    public static Keyboard of(List<KeyboardButton> buttons) {
        return Keyboard.builder()
                .buttons(new ArrayList<>(buttons))
                .build();
    }
}
