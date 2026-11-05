package io.github.bitrix24.bots.api.json;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom Jackson {@link Boolean} deserializer tailored for Bitrix24 REST API and webhook events.
 * <p>
 * In Bitrix24 webhook mode, events are serialized via PHP {@code http_build_query}, converting all scalar
 * values to strings:
 * <ul>
 *     <li>{@code boolean}: {@code true}/{@code false} in FETCH mode becomes {@code "1"}/{@code "0"} in Webhook mode</li>
 *     <li>{@code string|false}: {@code false} in FETCH mode becomes {@code "0"} in Webhook mode</li>
 *     <li>{@code null}: {@code null} in FETCH mode becomes {@code ""} in Webhook mode</li>
 *     <li>Legacy Bitrix24 flags: {@code "Y"}/{@code "N"}</li>
 * </ul>
 */
public class BitrixBooleanDeserializer extends ValueDeserializer<Boolean> {

    private final boolean primitive;

    public BitrixBooleanDeserializer() {
        this(false);
    }

    public BitrixBooleanDeserializer(boolean primitive) {
        this.primitive = primitive;
    }

    @Override
    public Boolean deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_TRUE) {
            return Boolean.TRUE;
        }
        if (token == JsonToken.VALUE_FALSE) {
            return Boolean.FALSE;
        }
        if (token == JsonToken.VALUE_NUMBER_INT) {
            return p.getLongValue() != 0L;
        }
        if (token == JsonToken.VALUE_NUMBER_FLOAT) {
            return p.getDoubleValue() != 0.0d;
        }
        if (token == JsonToken.VALUE_NULL) {
            return getNullValue(ctxt);
        }
        if (token == JsonToken.VALUE_STRING) {
            String text = p.getString().trim();
            if (text.isEmpty()) {
                return getNullValue(ctxt);
            }
            if ("0".equals(text)
                    || "false".equalsIgnoreCase(text)
                    || "N".equalsIgnoreCase(text)
                    || "no".equalsIgnoreCase(text)) {
                return Boolean.FALSE;
            }
            if ("1".equals(text)
                    || "true".equalsIgnoreCase(text)
                    || "Y".equalsIgnoreCase(text)
                    || "yes".equalsIgnoreCase(text)) {
                return Boolean.TRUE;
            }
            // Support Bitrix24 compound 'string | boolean' fields (e.g. User.idle, User.absent)
            // where false/"0" indicates false and an ISO-8601 date string indicates true.
            return Boolean.TRUE;
        }
        return getNullValue(ctxt);
    }

    @Override
    public Boolean getNullValue(DeserializationContext ctxt) {
        return primitive ? Boolean.FALSE : null;
    }
}
