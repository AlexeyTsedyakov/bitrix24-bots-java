package io.github.bitrix24.bots.sdk.polling;

import io.github.bitrix24.bots.api.client.BitrixApiClient;
import io.github.bitrix24.bots.api.exception.BitrixApiException;
import io.github.bitrix24.bots.api.model.common.BitrixResponse;
import io.github.bitrix24.bots.api.model.event.Event;
import io.github.bitrix24.bots.api.model.event.EventGetRequest;
import io.github.bitrix24.bots.api.model.event.EventGetResult;
import io.github.bitrix24.bots.sdk.config.PollingConfig;
import io.github.bitrix24.bots.sdk.dispatcher.BotEventDispatcher;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Robust background polling runner for Bitrix24 bots in FETCH mode ({@code imbot.v2.Event.get}).
 * Handles continuous queue polling, offset cursor management, immediate dispatching for backlog (hasMore),
 * and exponential backoff during network or server outages.
 */
@Slf4j
public class PollingBotRunner implements AutoCloseable {

    private final BitrixApiClient client;
    private final BotEventDispatcher dispatcher;
    private final PollingConfig config;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread pollingThread;
    private volatile Long currentOffset;

    /**
     * Constructs a new polling runner.
     *
     * @param client Bitrix24 REST API client
     * @param dispatcher event dispatcher
     * @param config polling configuration
     */
    public PollingBotRunner(BitrixApiClient client, BotEventDispatcher dispatcher, PollingConfig config) {
        this.client = Objects.requireNonNull(client, "client cannot be null");
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher cannot be null");
        this.config = config != null ? config : PollingConfig.builder().build();
        this.currentOffset = this.config.getInitialOffset();
    }

    /**
     * Starts the polling background thread.
     *
     * @return this runner instance
     */
    public synchronized PollingBotRunner start() {
        if (running.compareAndSet(false, true)) {
            log.info("Starting Bitrix24 bot polling runner with initial offset: {}", currentOffset);
            pollingThread = new Thread(this::pollLoop, "bitrix24-bot-polling");
            pollingThread.setDaemon(config.isDaemon());
            pollingThread.start();
        } else {
            log.warn("PollingBotRunner is already running");
        }
        return this;
    }

    /**
     * Blocks the calling thread until the polling runner stops or the calling thread is interrupted.
     *
     * @throws InterruptedException if the calling thread is interrupted while waiting
     */
    public void await() throws InterruptedException {
        Thread thread;
        synchronized (this) {
            thread = this.pollingThread;
        }
        if (thread != null) {
            thread.join();
        }
    }

    /**
     * Stops the polling background thread and waits for termination.
     */
    public synchronized void stop() {
        if (running.compareAndSet(true, false)) {
            log.info("Stopping Bitrix24 bot polling runner (current offset: {})", currentOffset);
            if (pollingThread != null) {
                pollingThread.interrupt();
                try {
                    pollingThread.join(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                pollingThread = null;
            }
        }
    }

    /**
     * Checks if the polling loop is active.
     *
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Returns the current event offset cursor.
     *
     * @return offset cursor or null
     */
    public Long getCurrentOffset() {
        return currentOffset;
    }

    /**
     * Manually updates the event offset cursor.
     *
     * @param offset new offset cursor
     */
    public void setCurrentOffset(Long offset) {
        this.currentOffset = offset;
    }

    /**
     * Performs a single poll cycle synchronously (useful for tests or controlled scheduling).
     *
     * @return true if there are more events immediately available (hasMore), false otherwise
     */
    public boolean pollOnce() {
        EventGetRequest request = EventGetRequest.builder()
                .offset(currentOffset)
                .limit(config.getLimit())
                .withUserEvents(config.isWithUserEvents())
                .botId(config.getBotId())
                .botToken(config.getBotToken())
                .build();

        BitrixResponse<EventGetResult> response = client.event().get(request);
        if (response == null) {
            throw new BitrixApiException("NULL_RESPONSE", "Received null response from imbot.v2.Event.get", 0, null, null);
        }

        EventGetResult result = response.getResultOrThrow();
        if (result == null) {
            return false;
        }

        // 1. Dispatch events in batch
        if (result.getEvents() != null && !result.getEvents().isEmpty()) {
            for (Event event : result.getEvents()) {
                if (!running.get() && pollingThread != null) {
                    break;
                }
                dispatcher.dispatch(event);
            }
        }

        // 2. Commit offset strictly after successful processing of events
        if (result.getNextOffset() != null) {
            this.currentOffset = result.getNextOffset();
        }

        return Boolean.TRUE.equals(result.getHasMore());
    }

    private void pollLoop() {
        long currentBackoff = config.getMinBackoffMs();

        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                boolean hasMore = pollOnce();

                // Reset backoff upon successful poll
                currentBackoff = config.getMinBackoffMs();

                // If backlog has more events, poll next batch immediately without delay
                if (hasMore) {
                    continue;
                }

                // Otherwise, wait for configured polling interval
                Thread.sleep(config.getIntervalMs());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Throwable t) {
                if (!running.get()) {
                    break;
                }
                log.warn("Error during imbot.v2.Event.get polling: {}. Retrying in {} ms", t.getMessage(), currentBackoff, t);
                dispatcher.notifyError(t);

                try {
                    Thread.sleep(currentBackoff);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                // Exponential backoff increase
                currentBackoff = Math.min((long) (currentBackoff * config.getBackoffMultiplier()), config.getMaxBackoffMs());
            }
        }
        log.info("PollingBotRunner loop finished");
    }

    @Override
    public void close() {
        stop();
    }
}
