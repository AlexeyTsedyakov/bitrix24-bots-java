package io.github.bitrix24.bots.api.json;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

/**
 * Custom Jackson {@link Instant} deserializer tailored for Bitrix24 REST API and webhook events.
 * <p>
 * Handles ISO-8601 strings with timezone offsets (e.g. {@code "2025-01-15T10:30:00+03:00"}),
 * UTC strings ({@code "2025-01-15T10:30:00Z"}), Unix epoch timestamps (numbers or strings),
 * and webhook empty strings {@code ""} or {@code "0"} / {@code false} as {@code null}.
 */
public class BitrixInstantDeserializer extends ValueDeserializer<Instant> {

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_NULL || token == JsonToken.VALUE_FALSE) {
            return null;
        }
        if (token == JsonToken.VALUE_NUMBER_INT) {
            return Instant.ofEpochSecond(p.getLongValue());
        }
        if (token == JsonToken.VALUE_NUMBER_FLOAT) {
            long millis = (long) (p.getDoubleValue() * 1000L);
            return Instant.ofEpochMilli(millis);
        }
        if (token == JsonToken.VALUE_STRING) {
            String text = p.getString().trim();
            if (text.isEmpty() || "0".equals(text) || "false".equalsIgnoreCase(text)) {
                return null;
            }
            try {
                return OffsetDateTime.parse(text).toInstant();
            } catch (DateTimeParseException ignored) {
                // fallback to Instant.parse or numeric epoch seconds
            }
            try {
                return Instant.parse(text);
            } catch (DateTimeParseException ignored) {
                // fallback to numeric epoch seconds
            }
            try {
                long epochSeconds = Long.parseLong(text);
                return Instant.ofEpochSecond(epochSeconds);
            } catch (NumberFormatException ignored) {
                // fallback to fractional epoch seconds
            }
            try {
                double epochSeconds = Double.parseDouble(text);
                return Instant.ofEpochMilli((long) (epochSeconds * 1000L));
            } catch (NumberFormatException e) {
                return (Instant) ctxt.handleWeirdStringValue(Instant.class, text,
                        "Cannot parse Bitrix24 timestamp to Instant");
            }
        }
        return null;
    }
}
