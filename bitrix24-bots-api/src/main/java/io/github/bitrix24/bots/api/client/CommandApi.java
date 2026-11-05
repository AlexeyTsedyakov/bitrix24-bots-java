package io.github.bitrix24.bots.api.client;

import io.github.bitrix24.bots.api.model.command.*;
import io.github.bitrix24.bots.api.model.common.BitrixResponse;

/**
 * Client interface for {@code imbot.v2.Command.*} methods.
 */
public interface CommandApi {

    /**
     * Registers a bot slash command via {@code imbot.v2.Command.register}.
     */
    BitrixResponse<CommandRegisterResult> register(CommandRegisterRequest request);

    default BitrixResponse<CommandRegisterResult> register(CommandRegisterFields fields) {
        return register(CommandRegisterRequest.of(fields));
    }

    default BitrixResponse<CommandRegisterResult> register(String command, String title) {
        return register(CommandRegisterFields.of(command, title));
    }

    /**
     * Unregisters/deletes a slash command via {@code imbot.v2.Command.unregister}.
     */
    BitrixResponse<Boolean> unregister(CommandUnregisterRequest request);

    default BitrixResponse<Boolean> unregister(Long commandId) {
        return unregister(CommandUnregisterRequest.of(commandId));
    }

    /**
     * Answers a slash command execution via {@code imbot.v2.Command.answer}.
     */
    BitrixResponse<Boolean> answer(CommandAnswerRequest request);

    default BitrixResponse<Boolean> answer(Long commandId, Long messageId, String dialogId, String message) {
        return answer(CommandAnswerRequest.of(commandId, messageId, dialogId, message));
    }

    default BitrixResponse<Boolean> answer(Long commandId, Long messageId, String dialogId, CommandAnswerFields fields) {
        return answer(CommandAnswerRequest.builder()
                .commandId(commandId)
                .messageId(messageId)
                .dialogId(dialogId)
                .fields(fields)
                .build());
    }
}
