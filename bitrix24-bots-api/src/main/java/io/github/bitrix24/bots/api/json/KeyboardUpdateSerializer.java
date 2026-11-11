package io.github.bitrix24.bots.api.json;

import io.github.bitrix24.bots.api.model.chat.keyboard.Keyboard;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class KeyboardUpdateSerializer extends ValueSerializer<Keyboard> {

    private final ValueSerializer<Object> defaultSerializer;

    public KeyboardUpdateSerializer() {
        this(null);
    }

    public KeyboardUpdateSerializer(ValueSerializer<Object> defaultSerializer) {
        this.defaultSerializer = defaultSerializer;
    }

    @Override
    public ValueSerializer<?> createContextual(SerializationContext ctxt, BeanProperty property) {
        ValueSerializer<Object> delegate = ctxt.findRootValueSerializer(Keyboard.class);
        return new KeyboardUpdateSerializer(delegate);
    }

    @Override
    public void serialize(Keyboard value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        if (value == null || value.isEmpty()) {
            gen.writeString("N");
        } else if (defaultSerializer != null) {
            defaultSerializer.serialize(value, gen, ctxt);
        } else {
            ctxt.findValueSerializer(Keyboard.class).serialize(value, gen, ctxt);
        }
    }

    @Override
    public boolean isEmpty(SerializationContext ctxt, Keyboard value) {
        return false;
    }
}