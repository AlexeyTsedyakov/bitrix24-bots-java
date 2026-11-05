package io.github.bitrix24.bots.api.client;

import java.io.IOException;
import java.util.Map;

/**
 * Interface for low-level HTTP communication with Bitrix24 REST API.
 */
public interface BitrixHttpClient extends AutoCloseable {

    /**
     * Executes an HTTP POST request with JSON body.
     *
     * @param url full URL to call
     * @param jsonBody JSON string to send in request body
     * @param headers additional headers (can be null or empty)
     * @return {@link BitrixHttpResponse} containing status code, headers and body
     * @throws IOException on network or connection errors
     */
    BitrixHttpResponse executePost(String url, String jsonBody, Map<String, String> headers) throws IOException;

    /**
     * Executes an HTTP POST request with JSON body without additional headers.
     *
     * @param url full URL to call
     * @param jsonBody JSON string to send in request body
     * @return {@link BitrixHttpResponse} containing status code, headers and body
     * @throws IOException on network or connection errors
     */
    default BitrixHttpResponse executePost(String url, String jsonBody) throws IOException {
        return executePost(url, jsonBody, null);
    }

    @Override
    default void close() {}
}
