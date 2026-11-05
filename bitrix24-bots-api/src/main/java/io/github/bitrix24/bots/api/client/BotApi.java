package io.github.bitrix24.bots.api.client;

import io.github.bitrix24.bots.api.model.bot.*;
import io.github.bitrix24.bots.api.model.common.BitrixResponse;

import java.util.Map;

/**
 * Client interface for {@code imbot.v2.Bot.*} methods.
 */
public interface BotApi {

    /**
     * Registers a new bot via {@code imbot.v2.Bot.register}.
     */
    BitrixResponse<BotRegisterResult> register(BotRegisterRequest request);

    default BitrixResponse<BotRegisterResult> register(BotRegisterFields fields) {
        return register(BotRegisterRequest.of(fields));
    }

    /**
     * Updates bot properties via {@code imbot.v2.Bot.update}.
     */
    BitrixResponse<BotUpdateResult> update(BotUpdateRequest request);

    default BitrixResponse<BotUpdateResult> update(Long botId, BotUpdateFields fields) {
        return update(BotUpdateRequest.of(botId, fields));
    }

    /**
     * Gets bot information via {@code imbot.v2.Bot.get}.
     */
    BitrixResponse<BotGetResult> get(BotGetRequest request);

    default BitrixResponse<BotGetResult> get(Long botId) {
        return get(BotGetRequest.ofId(botId));
    }

    default BitrixResponse<BotGetResult> get(String code) {
        return get(BotGetRequest.ofCode(code));
    }

    /**
     * Lists registered bots via {@code imbot.v2.Bot.list}.
     */
    BitrixResponse<BotListResult> list(BotListRequest request);

    default BitrixResponse<BotListResult> list() {
        return list(BotListRequest.builder().build());
    }

    default BitrixResponse<BotListResult> list(Map<String, Object> filter) {
        return list(BotListRequest.ofFilter(filter));
    }

    /**
     * Unregisters/deletes a bot via {@code imbot.v2.Bot.unregister}.
     */
    BitrixResponse<BotUnregisterResult> unregister(BotUnregisterRequest request);

    default BitrixResponse<BotUnregisterResult> unregister(Long botId) {
        return unregister(BotUnregisterRequest.of(botId));
    }
}
