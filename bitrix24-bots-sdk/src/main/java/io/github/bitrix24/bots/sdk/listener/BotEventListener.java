package io.github.bitrix24.bots.sdk.listener;

import io.github.bitrix24.bots.api.model.event.Event;

/**
 * Functional interface for receiving and processing Bitrix24 bot events.
 * Clients override {@link #onEvent(Event)} with their custom bot business logic.
 */
@FunctionalInterface
public interface BotEventListener {

    /**
     * Called when a verified and filtered Bitrix24 bot event is received.
     *
     * @param event incoming bot event
     */
    void onEvent(Event event);

    /**
     * Called when an uncaught error occurs during event dispatching or processing.
     * The default implementation is a no-op to prevent unhandled exceptions from crashing polling cycles.
     *
     * @param error caught throwable
     */
    default void onError(Throwable error) {
    }
}
