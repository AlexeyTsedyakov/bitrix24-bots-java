package io.github.bitrix24.bots.sdk.polling;

import io.github.bitrix24.bots.api.client.*;
import io.github.bitrix24.bots.api.exception.BitrixApiException;
import io.github.bitrix24.bots.api.model.common.BitrixResponse;
import io.github.bitrix24.bots.api.model.common.batch.BatchRequest;
import io.github.bitrix24.bots.api.model.common.batch.BatchResult;
import io.github.bitrix24.bots.api.model.event.Event;
import io.github.bitrix24.bots.api.model.event.EventGetRequest;
import io.github.bitrix24.bots.api.model.event.EventGetResult;
import io.github.bitrix24.bots.api.model.event.EventType;
import io.github.bitrix24.bots.sdk.config.BotConfig;
import io.github.bitrix24.bots.sdk.config.PollingConfig;
import io.github.bitrix24.bots.sdk.dispatcher.BotEventDispatcher;
import io.github.bitrix24.bots.sdk.listener.BotEventListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JavaType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class PollingBotRunnerTest {

    @Test
    @DisplayName("Single poll cycle fetches events and commits nextOffset")
    void testPollOnceSuccess() {
        StubEventApi eventApi = new StubEventApi();
        StubBitrixApiClient client = new StubBitrixApiClient(eventApi);

        List<Event> received = new ArrayList<>();
        BotEventDispatcher dispatcher = new BotEventDispatcher(BotConfig.builder().build(), received::add);

        PollingConfig pollingConfig = PollingConfig.builder()
                .initialOffset(100L)
                .limit(50)
                .build();

        // Queue a batch of events with nextOffset = 200
        Event event1 = Event.builder().eventId(101L).type(EventType.ONIMBOTV2MESSAGEADD).date(Instant.now()).build();
        Event event2 = Event.builder().eventId(102L).type(EventType.ONIMBOTV2COMMANDADD).date(Instant.now()).build();
        eventApi.queueResponse(BitrixResponse.ok(EventGetResult.builder()
                .events(List.of(event1, event2))
                .nextOffset(200L)
                .hasMore(false)
                .build()));

        PollingBotRunner runner = new PollingBotRunner(client, dispatcher, pollingConfig);
        assertThat(runner.getCurrentOffset()).isEqualTo(100L);

        boolean hasMore = runner.pollOnce();

        assertThat(hasMore).isFalse();
        assertThat(received).hasSize(2);
        assertThat(received.get(0).getType()).isEqualTo(EventType.ONIMBOTV2MESSAGEADD);
        assertThat(received.get(1).getType()).isEqualTo(EventType.ONIMBOTV2COMMANDADD);
        // Verify offset is committed to nextOffset
        assertThat(runner.getCurrentOffset()).isEqualTo(200L);

        // Verify request params
        assertThat(eventApi.recordedRequests).hasSize(1);
        EventGetRequest req = eventApi.recordedRequests.get(0);
        assertThat(req.getOffset()).isEqualTo(100L);
        assertThat(req.getLimit()).isEqualTo(50);
    }

    @Test
    @DisplayName("Returns hasMore true when Bitrix reports backlog, enabling immediate next poll")
    void testHasMoreFlag() {
        StubEventApi eventApi = new StubEventApi();
        StubBitrixApiClient client = new StubBitrixApiClient(eventApi);

        BotEventDispatcher dispatcher = new BotEventDispatcher();
        PollingConfig config = PollingConfig.builder().initialOffset(1L).build();

        eventApi.queueResponse(BitrixResponse.ok(EventGetResult.builder()
                .events(Collections.emptyList())
                .nextOffset(50L)
                .hasMore(true)
                .build()));

        PollingBotRunner runner = new PollingBotRunner(client, dispatcher, config);
        boolean hasMore = runner.pollOnce();

        assertThat(hasMore).isTrue();
        assertThat(runner.getCurrentOffset()).isEqualTo(50L);
    }

    @Test
    @DisplayName("Does not commit offset if API call returns an error response")
    void testErrorDoesNotCommitOffset() {
        StubEventApi eventApi = new StubEventApi();
        StubBitrixApiClient client = new StubBitrixApiClient(eventApi);

        BotEventDispatcher dispatcher = new BotEventDispatcher();
        PollingConfig config = PollingConfig.builder().initialOffset(100L).build();

        eventApi.queueResponse(BitrixResponse.error("BOT_NOT_FOUND", "Bot with this ID does not exist"));

        PollingBotRunner runner = new PollingBotRunner(client, dispatcher, config);

        try {
            runner.pollOnce();
        } catch (BitrixApiException e) {
            assertThat(e.getError()).isEqualTo("BOT_NOT_FOUND");
        }

        // Offset cursor must remain unchanged
        assertThat(runner.getCurrentOffset()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Continuous background polling loop processes multiple batches and stops cleanly")
    void testBackgroundPollingLifecycle() throws Exception {
        StubEventApi eventApi = new StubEventApi();
        StubBitrixApiClient client = new StubBitrixApiClient(eventApi);

        CountDownLatch eventsReceivedLatch = new CountDownLatch(3);
        List<Event> received = new CopyOnWriteArrayList<>();

        BotEventListener listener = event -> {
            received.add(event);
            eventsReceivedLatch.countDown();
        };

        BotEventDispatcher dispatcher = new BotEventDispatcher(BotConfig.builder().build(), listener);

        // Enqueue batch 1
        eventApi.queueResponse(BitrixResponse.ok(EventGetResult.builder()
                .events(List.of(
                        Event.builder().eventId(1L).type(EventType.ONIMBOTV2MESSAGEADD).date(Instant.now()).build(),
                        Event.builder().eventId(2L).type(EventType.ONIMBOTV2MESSAGEADD).date(Instant.now()).build()
                ))
                .nextOffset(3L)
                .hasMore(false)
                .build()));

        // Enqueue batch 2
        eventApi.queueResponse(BitrixResponse.ok(EventGetResult.builder()
                .events(List.of(
                        Event.builder().eventId(3L).type(EventType.ONIMBOTV2JOINCHAT).date(Instant.now()).build()
                ))
                .nextOffset(4L)
                .hasMore(false)
                .build()));

        // Enqueue empty batch 3
        eventApi.queueResponse(BitrixResponse.ok(EventGetResult.builder()
                .events(Collections.emptyList())
                .nextOffset(4L)
                .hasMore(false)
                .build()));

        PollingConfig config = PollingConfig.builder()
                .initialOffset(1L)
                .intervalMs(50L) // fast poll for test
                .build();

        PollingBotRunner runner = new PollingBotRunner(client, dispatcher, config);

        assertThat(runner.isRunning()).isFalse();
        runner.start();
        assertThat(runner.isRunning()).isTrue();

        boolean receivedInTime = eventsReceivedLatch.await(5, TimeUnit.SECONDS);
        assertThat(receivedInTime).isTrue();
        assertThat(received).hasSize(3);
        assertThat(runner.getCurrentOffset()).isEqualTo(4L);

        runner.stop();
        assertThat(runner.isRunning()).isFalse();
    }

    @Test
    @DisplayName("Network error in background loop notifies listener onError and applies exponential backoff")
    void testBackgroundPollingErrorBackoff() throws Exception {
        StubEventApi eventApi = new StubEventApi();
        StubBitrixApiClient client = new StubBitrixApiClient(eventApi);

        CountDownLatch errorLatch = new CountDownLatch(1);
        AtomicReference<Throwable> caughtError = new AtomicReference<>();

        BotEventListener listener = new BotEventListener() {
            @Override
            public void onEvent(Event event) {
            }

            @Override
            public void onError(Throwable error) {
                caughtError.set(error);
                errorLatch.countDown();
            }
        };

        BotEventDispatcher dispatcher = new BotEventDispatcher(BotConfig.builder().build(), listener);

        // Queue error response
        eventApi.queueResponse(BitrixResponse.error("QUERY_LIMIT_EXCEEDED", "Too many requests"));

        PollingConfig config = PollingConfig.builder()
                .initialOffset(1L)
                .intervalMs(50L)
                .minBackoffMs(50L)
                .maxBackoffMs(200L)
                .build();

        PollingBotRunner runner = new PollingBotRunner(client, dispatcher, config);
        runner.start();

        boolean gotError = errorLatch.await(5, TimeUnit.SECONDS);
        assertThat(gotError).isTrue();
        assertThat(caughtError.get()).isInstanceOf(BitrixApiException.class);

        runner.stop();
    }

    @Test
    @DisplayName("Polling thread runs as non-daemon by default to keep JVM alive and unblocks await() on stop")
    void testPollingThreadNonDaemonAndAwait() throws Exception {
        StubEventApi eventApi = new StubEventApi();
        StubBitrixApiClient client = new StubBitrixApiClient(eventApi);

        CountDownLatch pollStarted = new CountDownLatch(1);
        AtomicReference<Boolean> isDaemonThread = new AtomicReference<>();

        Event event = Event.builder().eventId(1L).type(EventType.ONIMBOTV2MESSAGEADD).date(Instant.now()).build();
        eventApi.queueResponse(BitrixResponse.ok(EventGetResult.builder()
                .events(List.of(event))
                .nextOffset(2L)
                .hasMore(false)
                .build()));

        BotEventDispatcher dispatcher = new BotEventDispatcher(BotConfig.builder().build(), ev -> {
            isDaemonThread.set(Thread.currentThread().isDaemon());
            pollStarted.countDown();
        });

        PollingConfig config = PollingConfig.builder().intervalMs(100L).build();
        PollingBotRunner runner = new PollingBotRunner(client, dispatcher, config);
        runner.start();

        assertThat(pollStarted.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(isDaemonThread.get()).isFalse();

        // Schedule stop from another thread and verify await() unblocks
        Thread stopper = new Thread(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
            runner.stop();
        });
        stopper.start();

        runner.await();
        assertThat(runner.isRunning()).isFalse();
    }

    // --- Test Stubs ---

    private static class StubEventApi implements EventApi {
        final List<EventGetRequest> recordedRequests = new ArrayList<>();
        final List<BitrixResponse<EventGetResult>> queuedResponses = new ArrayList<>();
        final AtomicInteger callIndex = new AtomicInteger(0);

        void queueResponse(BitrixResponse<EventGetResult> response) {
            queuedResponses.add(response);
        }

        @Override
        public BitrixResponse<EventGetResult> get(EventGetRequest request) {
            recordedRequests.add(request);
            int idx = callIndex.getAndIncrement();
            if (idx < queuedResponses.size()) {
                return queuedResponses.get(idx);
            }
            return BitrixResponse.ok(EventGetResult.builder().events(Collections.emptyList()).nextOffset(request.getOffset()).hasMore(false).build());
        }
    }

    private record StubBitrixApiClient(EventApi eventApi) implements BitrixApiClient {

        @Override
        public BitrixApiConfig getConfig() {
            return null;
        }

        @Override
        public BitrixHttpClient getHttpClient() {
            return null;
        }

        @Override
        public BotApi bot() {
            return null;
        }

        @Override
        public MessageApi message() {
            return null;
        }

        @Override
        public CommandApi command() {
            return null;
        }

        @Override
        public EventApi event() {
            return eventApi;
        }

        @Override
        public FileApi file() {
            return null;
        }

        @Override
        public <T> BitrixResponse<T> callMethod(String m, Object p, Class<T> r) {
            return null;
        }

        @Override
        public <T> BitrixResponse<T> callMethod(String m, Object p, TypeReference<T> r) {
            return null;
        }

        @Override
        public <T> BitrixResponse<T> callMethod(String m, Object p, JavaType r) {
            return null;
        }

        @Override
        public BitrixResponse<BatchResult> batch(BatchRequest request) {
            return null;
        }

        @Override
        public void close() {
        }
    }
}
