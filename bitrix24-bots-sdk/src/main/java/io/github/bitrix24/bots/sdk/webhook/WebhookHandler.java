package io.github.bitrix24.bots.sdk.webhook;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.bitrix24.bots.sdk.dispatcher.BotEventDispatcher;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * HTTP handler for processing incoming Bitrix24 webhook notifications.
 * Implements standard JDK {@link HttpHandler} for zero-dependency standalone operation
 * and provides framework-agnostic {@link #handle(String, String, String)} for Spring, Servlets, or Micronaut.
 */
@Slf4j
public class WebhookHandler implements HttpHandler {

    private static final int MAX_BODY_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB

    private final BotEventDispatcher dispatcher;

    /**
     * Constructs a webhook handler backed by the given event dispatcher.
     *
     * @param dispatcher bot event dispatcher
     */
    public WebhookHandler(BotEventDispatcher dispatcher) {
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher cannot be null");
    }

    /**
     * Standard JDK HttpServer entrypoint.
     *
     * @param exchange current HTTP exchange
     * @throws IOException on network I/O error
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");

        byte[] rawBytes;
        try (InputStream is = exchange.getRequestBody()) {
            rawBytes = is.readNBytes(MAX_BODY_SIZE_BYTES + 1);
        }

        WebhookResult result;
        if (rawBytes.length > MAX_BODY_SIZE_BYTES) {
            log.warn("Rejected webhook request exceeding maximum body size of {} bytes", MAX_BODY_SIZE_BYTES);
            result = new WebhookResult(413, "{\"error\":\"PAYLOAD_TOO_LARGE\",\"message\":\"Request body exceeds maximum allowed size\"}");
        } else {
            String body = new String(rawBytes, StandardCharsets.UTF_8);
            result = handle(method, contentType, body);
        }

        byte[] responseBytes = result.body().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(result.statusCode(), responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    /**
     * Framework-agnostic webhook processor.
     *
     * @param method      HTTP method (must be POST)
     * @param contentType Content-Type request header
     * @param requestBody raw request body
     * @return {@link WebhookResult} containing HTTP status code and response payload
     */
    public WebhookResult handle(String method, String contentType, String requestBody) {
        if (!"POST".equalsIgnoreCase(method)) {
            log.warn("Rejected webhook request with method: {}", method);
            return new WebhookResult(405, "{\"error\":\"METHOD_NOT_ALLOWED\",\"message\":\"Only POST is supported\"}");
        }

        if (requestBody == null || requestBody.isBlank()) {
            log.warn("Received empty webhook request body");
            return new WebhookResult(400, "{\"error\":\"EMPTY_BODY\",\"message\":\"Request body is empty\"}");
        }

        try {
            boolean isUrlEncoded = contentType != null && contentType.toLowerCase().contains("application/x-www-form-urlencoded");
            boolean dispatched;

            if (isUrlEncoded) {
                dispatched = dispatcher.dispatchWebhookForm(requestBody);
            } else {
                // Try JSON, fallback to URL-encoded if parsing as JSON fails
                try {
                    dispatched = dispatcher.dispatchWebhookJson(requestBody);
                } catch (Exception e) {
                    if (requestBody.contains("event=") || requestBody.contains("data[")) {
                        dispatched = dispatcher.dispatchWebhookForm(requestBody);
                    } else {
                        throw e;
                    }
                }
            }

            log.debug("Webhook successfully processed (dispatched={})", dispatched);
            return new WebhookResult(200, "{\"result\":\"OK\",\"dispatched\":" + dispatched + "}");

        } catch (SecurityException e) {
            log.warn("Webhook security verification failed: {}", e.getMessage());
            return new WebhookResult(401, "{\"error\":\"INVALID_APPLICATION_TOKEN\",\"message\":\"" + escapeJson(e.getMessage()) + "\"}");

        } catch (Throwable t) {
            log.error("Failed to process incoming webhook: {}", t.getMessage(), t);
            dispatcher.notifyError(t);
            return new WebhookResult(400, "{\"error\":\"BAD_REQUEST\",\"message\":\"" + escapeJson(t.getMessage()) + "\"}");
        }
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("\t", "\\t");
    }

    /**
     * Value representation of webhook handling outcome.
     */
    public record WebhookResult(int statusCode, String body) {
        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }
    }
}
