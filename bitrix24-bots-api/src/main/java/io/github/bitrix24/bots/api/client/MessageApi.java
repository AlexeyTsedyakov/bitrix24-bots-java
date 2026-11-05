package io.github.bitrix24.bots.api.client;

import io.github.bitrix24.bots.api.model.chat.*;
import io.github.bitrix24.bots.api.model.common.BitrixResponse;

/**
 * Client interface for {@code imbot.v2.Chat.Message.*} and related chat UI methods.
 */
public interface MessageApi {

    /**
     * Sends a message to a chat via {@code imbot.v2.Chat.Message.send}.
     */
    BitrixResponse<SendMessageResult> send(SendMessageRequest request);

    default BitrixResponse<SendMessageResult> send(String dialogId, String message) {
        return send(SendMessageRequest.of(dialogId, message));
    }

    default BitrixResponse<SendMessageResult> send(String dialogId, SendMessageFields fields) {
        return send(SendMessageRequest.of(dialogId, fields));
    }

    /**
     * Updates an existing message via {@code imbot.v2.Chat.Message.update}.
     */
    BitrixResponse<Boolean> update(UpdateMessageRequest request);

    default BitrixResponse<Boolean> update(Long messageId, String newMessage) {
        return update(UpdateMessageRequest.of(messageId, newMessage));
    }

    default BitrixResponse<Boolean> update(Long messageId, UpdateMessageFields fields) {
        return update(UpdateMessageRequest.of(messageId, fields));
    }

    /**
     * Deletes a message via {@code imbot.v2.Chat.Message.delete}.
     */
    BitrixResponse<Boolean> delete(DeleteMessageRequest request);

    default BitrixResponse<Boolean> delete(Long messageId) {
        return delete(DeleteMessageRequest.of(messageId));
    }

    default BitrixResponse<Boolean> delete(Long messageId, boolean complete) {
        return delete(DeleteMessageRequest.of(messageId, complete));
    }

    /**
     * Marks messages as read via {@code imbot.v2.Chat.Message.read}.
     */
    BitrixResponse<ReadMessageResult> read(ReadMessageRequest request);

    default BitrixResponse<ReadMessageResult> read(String dialogId) {
        return read(ReadMessageRequest.of(dialogId));
    }

    default BitrixResponse<ReadMessageResult> read(String dialogId, Long messageId) {
        return read(ReadMessageRequest.of(dialogId, messageId));
    }

    /**
     * Sends typing/input action notification via {@code imbot.v2.Chat.InputAction.notify}.
     */
    BitrixResponse<Boolean> sendTyping(SendTypingRequest request);

    default BitrixResponse<Boolean> sendTyping(String dialogId) {
        return sendTyping(SendTypingRequest.of(dialogId));
    }

    default BitrixResponse<Boolean> sendTyping(String dialogId, int durationSeconds) {
        return sendTyping(SendTypingRequest.of(dialogId, durationSeconds));
    }
}
