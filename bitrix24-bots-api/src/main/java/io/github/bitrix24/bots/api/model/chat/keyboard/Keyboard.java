package io.github.bitrix24.bots.api.model.chat.keyboard;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

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
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Keyboard {

    @JsonProperty("BUTTONS")
    @Builder.Default
    private List<KeyboardButton> buttons = new ArrayList<>();

    public List<KeyboardButton> getButtons() {
        return buttons;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return buttons == null || buttons.isEmpty();
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
