package io.github.bitrix24.bots.api.client;

import io.github.bitrix24.bots.api.model.common.BitrixResponse;
import io.github.bitrix24.bots.api.model.common.batch.BatchRequest;
import io.github.bitrix24.bots.api.model.common.batch.BatchResult;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JavaType;

import java.util.Map;

/**
 * Universal high-level client for Bitrix24 REST API and {@code imbot.v2} protocols.
 */
public interface BitrixApiClient extends AutoCloseable {

    /**
     * Returns the API configuration used by this client.
     */
    BitrixApiConfig getConfig();

    /**
     * Returns the underlying HTTP client.
     */
    BitrixHttpClient getHttpClient();

    /**
     * Accesses bot lifecycle methods ({@code imbot.v2.Bot.*}).
     */
    BotApi bot();

    /**
     * Accesses chat and message methods ({@code imbot.v2.Chat.Message.*} and input actions).
     */
    MessageApi message();

    /**
     * Accesses slash command methods ({@code imbot.v2.Command.*}).
     */
    CommandApi command();

    /**
     * Accesses polling event queue methods ({@code imbot.v2.Event.get}).
     */
    EventApi event();

    /**
     * Accesses chat file upload and download methods ({@code imbot.v2.File.*}).
     */
    FileApi file();

    /**
     * Generic call to any Bitrix24 REST API method.
     *
     * @param method REST method name (e.g. {@code "imbot.v2.Bot.get"}, {@code "crm.lead.list"})
     * @param params request parameters (DTO, Map, or null)
     * @param responseType expected type of {@code result} payload
     * @param <T> payload type
     * @return typed {@link BitrixResponse}
     */
    <T> BitrixResponse<T> callMethod(String method, Object params, Class<T> responseType);

    /**
     * Generic call to any Bitrix24 REST API method with complex type reference.
     */
    <T> BitrixResponse<T> callMethod(String method, Object params, TypeReference<T> responseType);

    /**
     * Generic call to any Bitrix24 REST API method with Jackson {@link JavaType}.
     */
    <T> BitrixResponse<T> callMethod(String method, Object params, JavaType responseType);

    /**
     * Executes a batch request containing multiple REST operations.
     *
     * @param request batch request
     * @return batch response containing results and errors per command
     */
    BitrixResponse<BatchResult> batch(BatchRequest request);

    default BitrixResponse<BatchResult> batch(Map<String, Object> cmd) {
        return batch(BatchRequest.of(cmd));
    }

    default BitrixResponse<BatchResult> batch(Map<String, Object> cmd, boolean halt) {
        return batch(BatchRequest.builder().cmd(cmd).halt(halt).build());
    }

    /**
     * Creates a new client with the given configuration and default HTTP client.
     */
    static BitrixApiClient create(BitrixApiConfig config) {
        return new DefaultBitrixApiClient(config);
    }

    /**
     * Creates a new client with the given configuration and custom HTTP client.
     */
    static BitrixApiClient create(BitrixApiConfig config, BitrixHttpClient httpClient) {
        return new DefaultBitrixApiClient(config, httpClient);
    }

    @Override
    default void close() {
        if (getHttpClient() != null) {
            getHttpClient().close();
        }
    }
}
