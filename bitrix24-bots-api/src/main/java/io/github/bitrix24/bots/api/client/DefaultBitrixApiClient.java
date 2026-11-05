package io.github.bitrix24.bots.api.client;

import io.github.bitrix24.bots.api.exception.BitrixApiException;
import io.github.bitrix24.bots.api.json.BitrixJsonMapper;
import io.github.bitrix24.bots.api.model.bot.*;
import io.github.bitrix24.bots.api.model.chat.*;
import io.github.bitrix24.bots.api.model.command.CommandAnswerRequest;
import io.github.bitrix24.bots.api.model.command.CommandRegisterRequest;
import io.github.bitrix24.bots.api.model.command.CommandRegisterResult;
import io.github.bitrix24.bots.api.model.command.CommandUnregisterRequest;
import io.github.bitrix24.bots.api.model.common.BitrixResponse;
import io.github.bitrix24.bots.api.model.common.BitrixTime;
import io.github.bitrix24.bots.api.model.common.batch.BatchRequest;
import io.github.bitrix24.bots.api.model.common.batch.BatchResult;
import io.github.bitrix24.bots.api.model.event.EventGetRequest;
import io.github.bitrix24.bots.api.model.event.EventGetResult;
import io.github.bitrix24.bots.api.model.file.FileDownloadRequest;
import io.github.bitrix24.bots.api.model.file.FileDownloadResult;
import io.github.bitrix24.bots.api.model.file.FileUploadRequest;
import io.github.bitrix24.bots.api.model.file.FileUploadResult;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default implementation of {@link BitrixApiClient}.
 */
@Slf4j
public class DefaultBitrixApiClient implements BitrixApiClient {

    private final BitrixApiConfig config;
    private final BitrixHttpClient httpClient;

    private final BotApi botApi;
    private final MessageApi messageApi;
    private final CommandApi commandApi;
    private final EventApi eventApi;
    private final FileApi fileApi;

    public DefaultBitrixApiClient(BitrixApiConfig config) {
        this(config, new OkHttpBitrixHttpClient());
    }

    public DefaultBitrixApiClient(BitrixApiConfig config, BitrixHttpClient httpClient) {
        this.config = Objects.requireNonNull(config, "config cannot be null");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient cannot be null");

        this.botApi = new DefaultBotApi();
        this.messageApi = new DefaultMessageApi();
        this.commandApi = new DefaultCommandApi();
        this.eventApi = new DefaultEventApi();
        this.fileApi = new DefaultFileApi();
    }

    @Override
    public BitrixApiConfig getConfig() {
        return config;
    }

    @Override
    public BitrixHttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    public BotApi bot() {
        return botApi;
    }

    @Override
    public MessageApi message() {
        return messageApi;
    }

    @Override
    public CommandApi command() {
        return commandApi;
    }

    @Override
    public EventApi event() {
        return eventApi;
    }

    @Override
    public FileApi file() {
        return fileApi;
    }

    @Override
    public <T> BitrixResponse<T> callMethod(String method, Object params, Class<T> responseType) {
        JavaType javaType = BitrixJsonMapper.getMapper().getTypeFactory().constructType(responseType);
        return callMethod(method, params, javaType);
    }

    @Override
    public <T> BitrixResponse<T> callMethod(String method, Object params, TypeReference<T> responseType) {
        JavaType javaType = BitrixJsonMapper.getMapper().getTypeFactory().constructType(responseType);
        return callMethod(method, params, javaType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> BitrixResponse<T> callMethod(String method, Object params, JavaType responseType) {
        Objects.requireNonNull(method, "method cannot be null");
        Objects.requireNonNull(responseType, "responseType cannot be null");

        String url = config.getMethodUrl(method);
        JsonNode requestNode = prepareParamsNode(method, params);
        String jsonPayload = BitrixJsonMapper.toJson(requestNode);

        Map<String, String> headers = new HashMap<>();
        if (config.getOauthToken() != null && !config.getOauthToken().isBlank()) {
            headers.put("Authorization", "Bearer " + config.getOauthToken());
        }

        BitrixHttpResponse httpResponse;
        try {
            httpResponse = httpClient.executePost(url, jsonPayload, headers);
        } catch (IOException e) {
            throw new BitrixApiException("NETWORK_ERROR", "Failed to communicate with Bitrix24 REST API: " + e.getMessage(), 0, null, e);
        }

        String responseBody = httpResponse.getBody();
        if (responseBody == null || responseBody.isBlank()) {
            throw new BitrixApiException("EMPTY_RESPONSE", "Received empty response from Bitrix24", httpResponse.getStatusCode(), null, null);
        }

        JsonNode root;
        try {
            root = BitrixJsonMapper.readTree(responseBody);
        } catch (Exception e) {
            throw new BitrixApiException("JSON_PARSE_ERROR", "Failed to parse Bitrix24 response as JSON: " + responseBody, httpResponse.getStatusCode(), null, e);
        }

        if (root.has("error")) {
            String error = root.path("error").asString(null);
            String errorDesc = root.path("error_description").asString(null);
            JsonNode errInfoNode = root.get("error_information");
            BitrixTime time = root.has("time")
                    ? BitrixJsonMapper.convertValue(root.get("time"), BitrixTime.class)
                    : null;

            return BitrixResponse.<T>builder()
                    .error(error)
                    .errorDescription(errorDesc)
                    .errorInformation(errInfoNode)
                    .time(time)
                    .build();
        }

        if (!httpResponse.isSuccessful()) {
            throw new BitrixApiException("HTTP_" + httpResponse.getStatusCode(),
                    "HTTP call failed with code " + httpResponse.getStatusCode() + ": " + responseBody,
                    httpResponse.getStatusCode(), null, null);
        }

        JsonNode resultNode = root.get("result");
        T resultPayload = null;

        if (resultNode != null && !resultNode.isNull()) {
            try {
                if (isBooleanType(responseType)) {
                    if (resultNode.isBoolean()) {
                        resultPayload = (T) Boolean.valueOf(resultNode.asBoolean());
                    } else if (resultNode.isObject() && resultNode.has("result")) {
                        resultPayload = (T) Boolean.valueOf(resultNode.get("result").asBoolean());
                    } else {
                        resultPayload = (T) Boolean.valueOf(resultNode.asBoolean());
                    }
                } else {
                    resultPayload = BitrixJsonMapper.getMapper().readValue(
                            BitrixJsonMapper.getMapper().treeAsTokens(resultNode), responseType);
                }
            } catch (Exception e) {
                throw new BitrixApiException("DESERIALIZATION_ERROR",
                        "Failed to deserialize Bitrix24 result payload into " + responseType + ": " + e.getMessage(),
                        httpResponse.getStatusCode(), null, e);
            }
        }

        BitrixTime time = root.has("time")
                ? BitrixJsonMapper.convertValue(root.get("time"), BitrixTime.class)
                : null;
        Integer total = root.has("total") ? root.get("total").asInt() : null;
        Integer next = root.has("next") ? root.get("next").asInt() : null;

        return BitrixResponse.<T>builder()
                .result(resultPayload)
                .time(time)
                .total(total)
                .next(next)
                .build();
    }

    @Override
    public BitrixResponse<BatchResult> batch(BatchRequest request) {
        return callMethod("batch", request, BatchResult.class);
    }

    private boolean isBooleanType(JavaType javaType) {
        Class<?> raw = javaType.getRawClass();
        return raw == Boolean.class || raw == boolean.class;
    }

    private JsonNode prepareParamsNode(String method, Object params) {
        ObjectNode objNode;
        if (params == null) {
            objNode = BitrixJsonMapper.getMapper().createObjectNode();
        } else if (params instanceof ObjectNode on) {
            objNode = on.deepCopy();
        } else if (params instanceof JsonNode jn && jn.isObject()) {
            objNode = ((ObjectNode) jn).deepCopy();
        } else {
            JsonNode converted = BitrixJsonMapper.getMapper().valueToTree(params);
            if (converted instanceof ObjectNode on) {
                objNode = on;
            } else {
                return converted;
            }
        }

        boolean isImbot = method.startsWith("imbot.v2.");

        // Inject botId if configured and not explicitly provided
        if (config.getBotId() != null && isImbot && !objNode.has("botId")) {
            if (!"imbot.v2.Bot.register".equals(method)) {
                objNode.put("botId", config.getBotId());
            }
        }

        // Inject botToken if configured and not explicitly provided
        if (config.getBotToken() != null && isImbot) {
            if ("imbot.v2.Bot.register".equals(method)) {
                JsonNode fieldsNode = objNode.get("fields");
                if (fieldsNode instanceof ObjectNode fn && !fn.has("botToken")) {
                    fn.put("botToken", config.getBotToken());
                }
            } else if (!objNode.has("botToken")) {
                objNode.put("botToken", config.getBotToken());
            }
        }

        // Inject auth OAuth token if configured and not present
        if (config.getOauthToken() != null && !config.getOauthToken().isBlank() && !objNode.has("auth")) {
            objNode.put("auth", config.getOauthToken());
        }

        return objNode;
    }

    private class DefaultBotApi implements BotApi {
        @Override
        public BitrixResponse<BotRegisterResult> register(BotRegisterRequest request) {
            return callMethod("imbot.v2.Bot.register", request, BotRegisterResult.class);
        }

        @Override
        public BitrixResponse<BotUpdateResult> update(BotUpdateRequest request) {
            return callMethod("imbot.v2.Bot.update", request, BotUpdateResult.class);
        }

        @Override
        public BitrixResponse<BotGetResult> get(BotGetRequest request) {
            return callMethod("imbot.v2.Bot.get", request, BotGetResult.class);
        }

        @Override
        public BitrixResponse<BotListResult> list(BotListRequest request) {
            return callMethod("imbot.v2.Bot.list", request, BotListResult.class);
        }

        @Override
        public BitrixResponse<BotUnregisterResult> unregister(BotUnregisterRequest request) {
            return callMethod("imbot.v2.Bot.unregister", request, BotUnregisterResult.class);
        }
    }

    private class DefaultMessageApi implements MessageApi {
        @Override
        public BitrixResponse<SendMessageResult> send(SendMessageRequest request) {
            return callMethod("imbot.v2.Chat.Message.send", request, SendMessageResult.class);
        }

        @Override
        public BitrixResponse<Boolean> update(UpdateMessageRequest request) {
            return callMethod("imbot.v2.Chat.Message.update", request, Boolean.class);
        }

        @Override
        public BitrixResponse<Boolean> delete(DeleteMessageRequest request) {
            return callMethod("imbot.v2.Chat.Message.delete", request, Boolean.class);
        }

        @Override
        public BitrixResponse<ReadMessageResult> read(ReadMessageRequest request) {
            return callMethod("imbot.v2.Chat.Message.read", request, ReadMessageResult.class);
        }

        @Override
        public BitrixResponse<Boolean> sendTyping(SendTypingRequest request) {
            return callMethod("imbot.v2.Chat.InputAction.notify", request, Boolean.class);
        }
    }

    private class DefaultCommandApi implements CommandApi {
        @Override
        public BitrixResponse<CommandRegisterResult> register(CommandRegisterRequest request) {
            return callMethod("imbot.v2.Command.register", request, CommandRegisterResult.class);
        }

        @Override
        public BitrixResponse<Boolean> unregister(CommandUnregisterRequest request) {
            return callMethod("imbot.v2.Command.unregister", request, Boolean.class);
        }

        @Override
        public BitrixResponse<Boolean> answer(CommandAnswerRequest request) {
            return callMethod("imbot.v2.Command.answer", request, Boolean.class);
        }
    }

    private class DefaultEventApi implements EventApi {
        @Override
        public BitrixResponse<EventGetResult> get(EventGetRequest request) {
            return callMethod("imbot.v2.Event.get", request, EventGetResult.class);
        }
    }

    private class DefaultFileApi implements FileApi {
        @Override
        public BitrixResponse<FileUploadResult> upload(FileUploadRequest request) {
            return callMethod("imbot.v2.File.upload", request, FileUploadResult.class);
        }

        @Override
        public BitrixResponse<FileDownloadResult> download(FileDownloadRequest request) {
            return callMethod("imbot.v2.File.download", request, FileDownloadResult.class);
        }
    }
}
