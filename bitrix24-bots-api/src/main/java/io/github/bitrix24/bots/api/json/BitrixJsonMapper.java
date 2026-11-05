package io.github.bitrix24.bots.api.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.bitrix24.bots.api.exception.BitrixApiException;
import io.github.bitrix24.bots.api.model.common.BitrixResponse;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.*;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.type.LogicalType;

import java.io.InputStream;
import java.time.Instant;

/**
 * Pre-configured Jackson 3 JSON mapper for Bitrix24 REST API.
 * The default mapper preserves standard camelCase Java property names used by modern imbot.v2 APIs,
 * while envelope metadata fields in snake_case (e.g. error_description, date_start) are mapped via explicit annotations.
 * Configured with built-in JSR-310 Java Time support, custom Bitrix24 scalar deserializers (supporting webhook string coercion
 * such as {@code "1"}/{@code "0"} for booleans and {@code ""} for nulls), disabled {@code FAIL_ON_UNKNOWN_PROPERTIES},
 * and case-insensitive properties.
 */
public final class BitrixJsonMapper {

    private static final ObjectMapper SHARED_MAPPER = createMapper();

    private BitrixJsonMapper() {
        // utility class
    }

    /**
     * Creates a Jackson {@link SimpleModule} with custom deserializers for Bitrix24 types
     * (such as webhook {@code "1"}/{@code "0"} booleans and flexible {@link Instant} formats).
     *
     * @return configured SimpleModule
     */
    public static SimpleModule createBitrixModule() {
        SimpleModule module = new SimpleModule("Bitrix24JsonModule");
        module.addDeserializer(Boolean.class, new BitrixBooleanDeserializer(false));
        module.addDeserializer(Boolean.TYPE, new BitrixBooleanDeserializer(true));
        module.addDeserializer(Instant.class, new BitrixInstantDeserializer());
        return module;
    }

    /**
     * Creates and configures a new {@link ObjectMapper} tailored for Bitrix24 imbot.v2 REST API.
     * Uses default camelCase naming for imbot.v2 wire contract.
     *
     * @return configured ObjectMapper
     */
    public static ObjectMapper createMapper() {
        return configureBaseBuilder(JsonMapper.builder()).build();
    }

    /**
     * Creates a specialized mapper configured with {@link PropertyNamingStrategies#SNAKE_CASE}
     * for legacy or CRM Bitrix24 REST API endpoints where field names follow snake_case.
     *
     * @return configured snake_case ObjectMapper
     */
    public static ObjectMapper createSnakeCaseMapper() {
        return configureBaseBuilder(JsonMapper.builder())
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();
    }

    private static JsonMapper.Builder configureBaseBuilder(JsonMapper.Builder builder) {
        return builder
                .addModule(createBitrixModule())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                .enable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
                .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .enable(EnumFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .changeDefaultPropertyInclusion(incl -> incl
                        .withValueInclusion(JsonInclude.Include.NON_NULL)
                        .withContentInclusion(JsonInclude.Include.NON_NULL))
                .withCoercionConfig(LogicalType.Integer, cfg ->
                        cfg.setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull))
                .withCoercionConfig(LogicalType.Float, cfg ->
                        cfg.setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull))
                .withCoercionConfig(LogicalType.Boolean, cfg ->
                        cfg.setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull))
                .withCoercionConfig(LogicalType.DateTime, cfg ->
                        cfg.setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull))
                .withCoercionConfig(LogicalType.Collection, cfg ->
                        cfg.setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull));
    }

    /**
     * Returns the shared singleton {@link ObjectMapper} instance.
     *
     * @return shared ObjectMapper
     */
    public static ObjectMapper getMapper() {
        return SHARED_MAPPER;
    }

    /**
     * Serializes an object into a JSON string.
     *
     * @param value object to serialize
     * @return JSON string
     * @throws BitrixApiException if serialization fails
     */
    public static String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return SHARED_MAPPER.writeValueAsString(value);
        } catch (JacksonException e) {
            throw new BitrixApiException("Failed to serialize object to JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Deserializes a JSON string into an instance of the specified class.
     *
     * @param json JSON string
     * @param clazz target class
     * @param <T> result type
     * @return deserialized object
     * @throws BitrixApiException if deserialization fails
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return SHARED_MAPPER.readValue(json, clazz);
        } catch (JacksonException e) {
            throw new BitrixApiException("Failed to deserialize JSON to " + clazz.getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Deserializes a JSON string using a {@link TypeReference}.
     *
     * @param json JSON string
     * @param typeReference target type reference
     * @param <T> result type
     * @return deserialized object
     * @throws BitrixApiException if deserialization fails
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return SHARED_MAPPER.readValue(json, typeReference);
        } catch (JacksonException e) {
            throw new BitrixApiException("Failed to deserialize JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Deserializes a JSON string using a Jackson {@link JavaType}.
     *
     * @param json JSON string
     * @param javaType target java type
     * @param <T> result type
     * @return deserialized object
     * @throws BitrixApiException if deserialization fails
     */
    public static <T> T fromJson(String json, JavaType javaType) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return SHARED_MAPPER.readValue(json, javaType);
        } catch (JacksonException e) {
            throw new BitrixApiException("Failed to deserialize JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Deserializes an input stream into an instance of the specified class.
     *
     * @param inputStream input stream containing JSON
     * @param clazz target class
     * @param <T> result type
     * @return deserialized object
     * @throws BitrixApiException if deserialization fails
     */
    public static <T> T fromJson(InputStream inputStream, Class<T> clazz) {
        if (inputStream == null) {
            return null;
        }
        try {
            return SHARED_MAPPER.readValue(inputStream, clazz);
        } catch (JacksonException e) {
            throw new BitrixApiException("Failed to deserialize JSON stream: " + e.getMessage(), e);
        }
    }

    /**
     * Parses JSON string into a Jackson {@link JsonNode} tree.
     *
     * @param json JSON string
     * @return JsonNode tree
     * @throws BitrixApiException if parsing fails
     */
    public static JsonNode readTree(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return SHARED_MAPPER.readTree(json);
        } catch (JacksonException e) {
            throw new BitrixApiException("Failed to parse JSON tree: " + e.getMessage(), e);
        }
    }

    /**
     * Converts an arbitrary object or map into the target type.
     *
     * @param fromValue source object
     * @param toValueType target class
     * @param <T> target type
     * @return converted object
     */
    public static <T> T convertValue(Object fromValue, Class<T> toValueType) {
        return SHARED_MAPPER.convertValue(fromValue, toValueType);
    }

    /**
     * Converts an arbitrary object or map into the target type reference.
     *
     * @param fromValue source object
     * @param toValueTypeRef target type reference
     * @param <T> target type
     * @return converted object
     */
    public static <T> T convertValue(Object fromValue, TypeReference<T> toValueTypeRef) {
        return SHARED_MAPPER.convertValue(fromValue, toValueTypeRef);
    }

    /**
     * Helper to deserialize a Bitrix24 REST response wrapper containing a typed payload.
     *
     * @param json JSON response string
     * @param payloadClass class of the payload in the {@code result} field
     * @param <T> payload type
     * @return typed {@link BitrixResponse}
     */
    public static <T> BitrixResponse<T> parseResponse(String json, Class<T> payloadClass) {
        JavaType responseType = SHARED_MAPPER.getTypeFactory()
                .constructParametricType(BitrixResponse.class, payloadClass);
        return fromJson(json, responseType);
    }
}
