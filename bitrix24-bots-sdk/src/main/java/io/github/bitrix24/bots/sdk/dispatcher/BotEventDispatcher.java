package io.github.bitrix24.bots.sdk.dispatcher;

import io.github.bitrix24.bots.api.json.BitrixJsonMapper;
import io.github.bitrix24.bots.api.model.event.AuthInfo;
import io.github.bitrix24.bots.api.model.event.Event;
import io.github.bitrix24.bots.api.model.event.EventData;
import io.github.bitrix24.bots.api.model.event.EventType;
import io.github.bitrix24.bots.sdk.config.BotConfig;
import io.github.bitrix24.bots.sdk.listener.BotEventListener;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dispatches Bitrix24 bot events (from polling or webhooks) to registered {@link BotEventListener}s.
 * Performs application token verification, event filtering by {@link BotConfig#getAllowedEvents()},
 * safe payload mapping, and protected exception handling.
 */
@Slf4j
public class BotEventDispatcher {

    private static final Pattern FORM_KEY_PATTERN = Pattern.compile("([^^\\[\\]]+)|\\[([^\\[\\]]*)\\]");

    private final BotConfig config;
    private final List<BotEventListener> listeners = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;

    /**
     * Constructs dispatcher with default {@link BotConfig} and shared {@link ObjectMapper}.
     */
    public BotEventDispatcher() {
        this(BotConfig.builder().build(), null, BitrixJsonMapper.getMapper());
    }

    /**
     * Constructs dispatcher with specified configuration.
     *
     * @param config bot configuration
     */
    public BotEventDispatcher(BotConfig config) {
        this(config, null, BitrixJsonMapper.getMapper());
    }

    /**
     * Constructs dispatcher with specified configuration and initial listener.
     *
     * @param config bot configuration
     * @param listener initial event listener (optional)
     */
    public BotEventDispatcher(BotConfig config, BotEventListener listener) {
        this(config, listener, BitrixJsonMapper.getMapper());
    }

    /**
     * Constructs dispatcher with configuration, listener, and customized mapper.
     *
     * @param config bot configuration
     * @param listener initial event listener (optional)
     * @param objectMapper customized Jackson ObjectMapper
     */
    public BotEventDispatcher(BotConfig config, BotEventListener listener, ObjectMapper objectMapper) {
        this.config = config != null ? config : BotConfig.builder().build();
        this.objectMapper = objectMapper != null ? objectMapper : BitrixJsonMapper.getMapper();
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    /**
     * Adds an event listener.
     *
     * @param listener event listener
     */
    public void addListener(BotEventListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    /**
     * Removes an event listener.
     *
     * @param listener event listener
     */
    public void removeListener(BotEventListener listener) {
        if (listener != null) {
            this.listeners.remove(listener);
        }
    }

    /**
     * Replaces all registered listeners with the specified single listener.
     *
     * @param listener new event listener
     */
    public void setListener(BotEventListener listener) {
        this.listeners.clear();
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    /**
     * Returns an unmodifiable list of registered listeners.
     *
     * @return registered listeners
     */
    public List<BotEventListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    /**
     * Returns current bot configuration.
     *
     * @return bot configuration
     */
    public BotConfig getConfig() {
        return config;
    }

    /**
     * Dispatches an incoming {@link Event} (from polling queue or direct call).
     * Checks allowed events, validates application token, and forwards to listeners.
     *
     * @param event incoming event
     * @return true if event was permitted and dispatched, false if filtered out
     * @throws SecurityException if application token validation fails
     */
    public boolean dispatch(Event event) {
        if (event == null) {
            return false;
        }

        EventType type = event.getType() != null ? event.getType() : EventType.UNKNOWN;

        // Verify application token if event contains authentication data and token is configured
        if (event.getAuth() != null && event.getAuth().getApplicationToken() != null) {
            verifyApplicationToken(event.getAuth().getApplicationToken());
        }

        // Filter by allowed events
        if (!config.isAllowed(type)) {
            log.debug("Skipping event of type {} because it is not in allowedEvents", type);
            return false;
        }

        notifyListeners(event);
        return true;
    }

    /**
     * Parses and dispatches an incoming webhook JSON payload.
     * Performs top-level application token verification.
     *
     * @param json webhook payload JSON string
     * @return true if permitted and dispatched, false if filtered out
     * @throws SecurityException if application token is invalid
     */
    public boolean dispatchWebhookJson(String json) {
        if (json == null || json.isBlank()) {
            return false;
        }

        try {
            JsonNode root = objectMapper.readTree(json);
            return dispatchWebhookJsonNode(root);
        } catch (JacksonException e) {
            log.error("Failed to parse webhook JSON payload: {}", e.getMessage(), e);
            notifyError(e);
            return false;
        }
    }

    /**
     * Dispatches an incoming webhook parsed into a {@link JsonNode} tree.
     *
     * @param root root JSON node
     * @return true if permitted and dispatched, false if filtered out
     * @throws SecurityException if application token is invalid
     */
    public boolean dispatchWebhookJsonNode(JsonNode root) {
        if (root == null || root.isNull() || root.isMissingNode()) {
            return false;
        }

        // Extract top-level application_token for verification
        String incomingAppToken = extractApplicationToken(root);
        verifyApplicationToken(incomingAppToken);

        // Extract event type
        String eventName = null;
        if (root.hasNonNull("event")) {
            eventName = root.get("event").asString();
        } else if (root.hasNonNull("type")) {
            eventName = root.get("type").asString();
        }
        EventType type = EventType.fromString(eventName);

        // Fast filtering
        if (!config.isAllowed(type)) {
            log.debug("Skipping webhook event {} (type: {}) - not allowed", eventName, type);
            return false;
        }

        // Extract timestamp
        Instant date = Instant.now();
        if (root.hasNonNull("ts")) {
            JsonNode tsNode = root.get("ts");
            if (tsNode.isNumber()) {
                date = Instant.ofEpochSecond(tsNode.asLong());
            } else {
                try {
                    date = Instant.ofEpochSecond(Long.parseLong(tsNode.asString()));
                } catch (NumberFormatException ignored) {
                    // fallback to now
                }
            }
        }

        // Extract eventId if present
        Long eventId = null;
        if (root.hasNonNull("eventId")) {
            JsonNode eventIdNode = root.get("eventId");
            if (eventIdNode.isNumber()) {
                eventId = eventIdNode.asLong();
            } else {
                String eventIdText = eventIdNode.asString().trim();
                if (!eventIdText.isEmpty()) {
                    try {
                        eventId = Long.parseLong(eventIdText);
                    } catch (NumberFormatException ignored) {
                        // ignore unparseable eventId
                    }
                }
            }
        }

        // Extract data
        EventData data = null;
        JsonNode dataNode = root.get("data");
        if (dataNode != null && !dataNode.isNull() && !dataNode.isMissingNode()) {
            try {
                data = objectMapper.treeToValue(dataNode, EventData.class);
            } catch (JacksonException e) {
                log.error("Failed to deserialize webhook data node: {}", e.getMessage(), e);
                notifyError(e);
                return false;
            }
        }

        // Attach top-level auth info if data doesn't already have one
        JsonNode authNode = root.get("auth");
        if (authNode != null && !authNode.isNull() && !authNode.isMissingNode()) {
            try {
                AuthInfo topAuth = objectMapper.treeToValue(authNode, AuthInfo.class);
                if (data == null) {
                    data = EventData.builder().auth(topAuth).build();
                } else if (data.getAuth() == null) {
                    data.setAuth(topAuth);
                }
            } catch (JacksonException e) {
                log.warn("Failed to deserialize top-level auth node: {}", e.getMessage());
            }
        }

        Event event = Event.builder()
                .eventId(eventId)
                .type(type)
                .date(date)
                .data(data)
                .build();

        notifyListeners(event);
        return true;
    }

    /**
     * Parses and dispatches an incoming webhook in {@code application/x-www-form-urlencoded} format
     * generated by PHP {@code http_build_query}.
     *
     * @param formUrlEncoded urlencoded query body
     * @return true if permitted and dispatched, false if filtered out
     * @throws SecurityException if application token is invalid
     */
    public boolean dispatchWebhookForm(String formUrlEncoded) {
        if (formUrlEncoded == null || formUrlEncoded.isBlank()) {
            return false;
        }

        Map<String, Object> map = parseFormToMap(formUrlEncoded);
        JsonNode rootNode = objectMapper.valueToTree(map);
        return dispatchWebhookJsonNode(rootNode);
    }

    /**
     * Verifies that the incoming application token matches the configured {@link BotConfig#getApplicationToken()}.
     *
     * @param incomingToken token received from Bitrix24
     * @throws SecurityException if verification fails
     */
    public void verifyApplicationToken(String incomingToken) {
        String expectedToken = config.getApplicationToken();
        if (expectedToken != null && !expectedToken.isBlank()) {
            if (incomingToken == null || !Objects.equals(expectedToken.trim(), incomingToken.trim())) {
                String msg = "Bitrix24 application_token verification failed: received [" + incomingToken + "]";
                log.warn(msg);
                SecurityException secEx = new SecurityException(msg);
                notifyError(secEx);
                throw secEx;
            }
        }
    }

    private String extractApplicationToken(JsonNode root) {
        if (root == null) {
            return null;
        }
        // 1. Check top-level auth.application_token
        JsonNode topAuth = root.get("auth");
        if (topAuth != null && topAuth.hasNonNull("application_token")) {
            return topAuth.get("application_token").asString();
        }
        if (topAuth != null && topAuth.hasNonNull("applicationToken")) {
            return topAuth.get("applicationToken").asString();
        }

        // 2. Check data.bot.auth.application_token
        JsonNode dataNode = root.get("data");
        if (dataNode != null) {
            JsonNode botNode = dataNode.get("bot");
            if (botNode != null) {
                JsonNode botAuth = botNode.get("auth");
                if (botAuth != null && botAuth.hasNonNull("application_token")) {
                    return botAuth.get("application_token").asString();
                }
                if (botAuth != null && botAuth.hasNonNull("applicationToken")) {
                    return botAuth.get("applicationToken").asString();
                }
            }
        }
        return null;
    }

    private void notifyListeners(Event event) {
        for (BotEventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Throwable t) {
                log.error("Unhandled exception in BotEventListener.onEvent: {}", t.getMessage(), t);
                try {
                    listener.onError(t);
                } catch (Throwable err) {
                    log.error("Unhandled exception in BotEventListener.onError: {}", err.getMessage(), err);
                }
            }
        }
    }

    /**
     * Forwards an error to all registered listeners' onError callback.
     *
     * @param error caught throwable
     */
    public void notifyError(Throwable error) {
        for (BotEventListener listener : listeners) {
            try {
                listener.onError(error);
            } catch (Throwable err) {
                log.error("Unhandled exception in BotEventListener.onError: {}", err.getMessage(), err);
            }
        }
    }

    /**
     * Parses nested PHP bracket query string (e.g. {@code data[message][text]=hi&auth[application_token]=tok})
     * into a nested Java map hierarchy.
     *
     * @param queryString urlencoded query body
     * @return hierarchical map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseFormToMap(String queryString) {
        Map<String, Object> root = new LinkedHashMap<>();
        if (queryString == null || queryString.isBlank()) {
            return root;
        }

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            if (pair.isBlank()) {
                continue;
            }
            int idx = pair.indexOf('=');
            String rawKey = idx > 0 ? pair.substring(0, idx) : pair;
            String rawVal = idx > 0 && pair.length() > idx + 1 ? pair.substring(idx + 1) : "";

            String key = URLDecoder.decode(rawKey, StandardCharsets.UTF_8);
            String value = URLDecoder.decode(rawVal, StandardCharsets.UTF_8);

            List<String> tokens = parseKeyTokens(key);
            if (tokens.isEmpty()) {
                continue;
            }

            Map<String, Object> current = root;
            for (int i = 0; i < tokens.size() - 1; i++) {
                String token = tokens.get(i);
                if (token.isEmpty()) {
                    token = String.valueOf(current.size());
                }
                Object child = current.get(token);
                if (!(child instanceof Map)) {
                    Map<String, Object> nextMap = new LinkedHashMap<>();
                    current.put(token, nextMap);
                    current = nextMap;
                } else {
                    current = (Map<String, Object>) child;
                }
            }

            String leafToken = tokens.get(tokens.size() - 1);
            if (leafToken.isEmpty()) {
                leafToken = String.valueOf(current.size());
            }
            current.put(leafToken, value);
        }

        normalizeIndexedMaps(root);
        return root;
    }

    @SuppressWarnings("unchecked")
    private static void normalizeIndexedMaps(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof Map) {
                Map<String, Object> childMap = (Map<String, Object>) val;
                normalizeIndexedMaps(childMap);
                if (isSequentialNumericKeyMap(childMap)) {
                    List<Object> list = new ArrayList<>(childMap.size());
                    for (int i = 0; i < childMap.size(); i++) {
                        list.add(childMap.get(String.valueOf(i)));
                    }
                    entry.setValue(list);
                }
            }
        }
    }

    private static boolean isSequentialNumericKeyMap(Map<String, Object> map) {
        if (map.isEmpty()) {
            return false;
        }
        for (int i = 0; i < map.size(); i++) {
            if (!map.containsKey(String.valueOf(i))) {
                return false;
            }
        }
        return true;
    }

    private static List<String> parseKeyTokens(String key) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = FORM_KEY_PATTERN.matcher(key);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                tokens.add(matcher.group(1));
            } else if (matcher.group(2) != null) {
                tokens.add(matcher.group(2));
            }
        }
        return tokens;
    }
}
