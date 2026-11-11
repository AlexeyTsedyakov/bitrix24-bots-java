package io.github.bitrix24.bots.api.json;

import io.github.bitrix24.bots.api.model.chat.keyboard.Keyboard;

/**
 * Filter for @JsonInclude(CUSTOM): the value is excluded when equals() returns true.
 */
public class KeyboardEmptyFilter {

    @Override
    public boolean equals(Object obj) {
        return obj == null || (obj instanceof Keyboard k && k.isEmpty());
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
