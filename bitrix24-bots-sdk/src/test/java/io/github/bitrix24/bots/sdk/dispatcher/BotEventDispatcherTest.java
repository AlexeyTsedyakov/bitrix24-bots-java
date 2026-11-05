package io.github.bitrix24.bots.sdk.dispatcher;

import io.github.bitrix24.bots.api.json.BitrixJsonMapper;
import io.github.bitrix24.bots.api.model.event.Event;
import io.github.bitrix24.bots.api.model.event.EventType;
import io.github.bitrix24.bots.sdk.config.BotConfig;
import io.github.bitrix24.bots.sdk.listener.BotEventListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BotEventDispatcherTest {

    @Test
    @DisplayName("Dispatches all imbot.v2 event types when default configuration is used")
    void testDispatchAllEventTypesByDefault() {
        List<Event> received = new ArrayList<>();
        BotEventDispatcher dispatcher = new BotEventDispatcher(BotConfig.builder().build(), received::add);

        EventType[] types = {
                EventType.ONIMBOTV2MESSAGEADD,
                EventType.ONIMBOTV2COMMANDADD,
                EventType.ONIMBOTV2JOINCHAT,
                EventType.ONIMBOTV2DELETE,
                EventType.ONIMBOTV2MESSAGEUPDATE,
                EventType.ONIMBOTV2MESSAGEDELETE,
                EventType.ONIMBOTV2REACTIONCHANGE,
                EventType.ONIMBOTV2CONTEXTGET,
                EventType.UNKNOWN
        };

        for (EventType type : types) {
            boolean dispatched = dispatcher.dispatch(Event.builder().type(type).build());
            assertThat(dispatched).isTrue();
        }

        assertThat(received).hasSize(types.length);
        for (int i = 0; i < types.length; i++) {
            assertThat(received.get(i).getType()).isEqualTo(types[i]);
        }
    }

    @Test
    @DisplayName("Filters events based on custom allowedEvents configuration")
    void testFilterByAllowedEvents() {
        List<Event> received = new ArrayList<>();
        BotConfig config = BotConfig.builder()
                .allowedEvents(EnumSet.of(EventType.ONIMBOTV2MESSAGEADD, EventType.ONIMBOTV2COMMANDADD))
                .build();
        BotEventDispatcher dispatcher = new BotEventDispatcher(config, received::add);

        boolean messageDispatched = dispatcher.dispatch(Event.builder().type(EventType.ONIMBOTV2MESSAGEADD).build());
        boolean commandDispatched = dispatcher.dispatch(Event.builder().type(EventType.ONIMBOTV2COMMANDADD).build());
        boolean joinDispatched = dispatcher.dispatch(Event.builder().type(EventType.ONIMBOTV2JOINCHAT).build());
        boolean deleteDispatched = dispatcher.dispatch(Event.builder().type(EventType.ONIMBOTV2DELETE).build());

        assertThat(messageDispatched).isTrue();
        assertThat(commandDispatched).isTrue();
        assertThat(joinDispatched).isFalse();
        assertThat(deleteDispatched).isFalse();

        assertThat(received).hasSize(2);
        assertThat(received.get(0).getType()).isEqualTo(EventType.ONIMBOTV2MESSAGEADD);
        assertThat(received.get(1).getType()).isEqualTo(EventType.ONIMBOTV2COMMANDADD);
    }

    @Test
    @DisplayName("Dispatches Event from polling JSON and maps fields properly")
    void testDispatchPolledEvent() {
        List<Event> received = new ArrayList<>();
        BotEventDispatcher dispatcher = new BotEventDispatcher(BotConfig.builder().build(), received::add);

        String eventJson = """
                {
                    "eventId": 1001,
                    "type": "ONIMBOTV2MESSAGEADD",
                    "date": "2025-01-15T10:30:00Z",
                    "data": {
                        "message": {
                            "id": 789,
                            "chatId": 5,
                            "text": "Hello world from test"
                        },
                        "chat": {
                            "id": "5",
                            "dialogId": "chat5"
                        },
                        "user": {
                            "id": 42,
                            "name": "Ivan Ivanov"
                        }
                    }
                }
                """;

        Event polledEvent = BitrixJsonMapper.fromJson(eventJson, Event.class);

        boolean result = dispatcher.dispatch(polledEvent);

        assertThat(result).isTrue();
        assertThat(received).hasSize(1);
        Event event = received.get(0);
        assertThat(event.getEventId()).isEqualTo(1001L);
        assertThat(event.getType()).isEqualTo(EventType.ONIMBOTV2MESSAGEADD);
        assertThat(event.getDate()).isEqualTo(Instant.parse("2025-01-15T10:30:00Z"));
        assertThat(event.isMessage()).isTrue();
        assertThat(event.getMessageText()).isEqualTo("Hello world from test");
        assertThat(event.getChatId()).isEqualTo("5");
        assertThat(event.getDialogId()).isEqualTo("chat5");
        assertThat(event.getUserId()).isEqualTo(42L);
        assertThat(event.getUserName()).isEqualTo("Ivan Ivanov");
    }

    @Test
    @DisplayName("Dispatches Webhook JSON payload and verifies application token")
    void testDispatchWebhookJsonSuccess() {
        List<Event> received = new ArrayList<>();
        BotConfig config = BotConfig.builder()
                .applicationToken("valid_secret_token_123")
                .build();
        BotEventDispatcher dispatcher = new BotEventDispatcher(config, received::add);

        String webhookJson = """
                {
                    "event": "ONIMBOTV2COMMANDADD",
                    "data": {
                        "bot": {
                            "id": 10,
                            "code": "mybot"
                        },
                        "command": {
                            "id": 55,
                            "command": "/help",
                            "params": "general"
                        },
                        "chat": {
                            "id": "12",
                            "dialogId": "chat12"
                        }
                    },
                    "ts": 1736937000,
                    "auth": {
                        "domain": "portal.bitrix24.ru",
                        "application_token": "valid_secret_token_123"
                    }
                }
                """;

        boolean dispatched = dispatcher.dispatchWebhookJson(webhookJson);

        assertThat(dispatched).isTrue();
        assertThat(received).hasSize(1);
        Event event = received.get(0);
        assertThat(event.isCommand()).isTrue();
        assertThat(event.getCommandName()).isEqualTo("/help");
        assertThat(event.getCommandParams()).isEqualTo("general");
        assertThat(event.getBotId()).isEqualTo(10L);
        assertThat(event.getChatId()).isEqualTo("12");
        assertThat(event.getAuth()).isNotNull();
        assertThat(event.getAuth().getApplicationToken()).isEqualTo("valid_secret_token_123");
        assertThat(event.getAuth().getDomain()).isEqualTo("portal.bitrix24.ru");
    }

    @Test
    @DisplayName("Rejects Webhook JSON when application token does not match")
    void testDispatchWebhookJsonInvalidToken() {
        BotConfig config = BotConfig.builder()
                .applicationToken("expected_token")
                .build();
        BotEventDispatcher dispatcher = new BotEventDispatcher(config);

        String webhookJson = """
                {
                    "event": "ONIMBOTV2MESSAGEADD",
                    "auth": {
                        "application_token": "wrong_token"
                    }
                }
                """;

        assertThatThrownBy(() -> dispatcher.dispatchWebhookJson(webhookJson))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Bitrix24 application_token verification failed")
                .hasMessageContaining("wrong_token")
                .hasMessageNotContaining("expected_token");
    }

    @Test
    @DisplayName("Dispatches Webhook urlencoded form format from PHP http_build_query")
    void testDispatchWebhookForm() {
        List<Event> received = new ArrayList<>();
        BotConfig config = BotConfig.builder()
                .applicationToken("form_secret_token")
                .build();
        BotEventDispatcher dispatcher = new BotEventDispatcher(config, received::add);

        String formUrlEncoded = "event=ONIMBOTV2MESSAGEADD"
                + "&ts=1736937000"
                + "&auth%5Bapplication_token%5D=form_secret_token"
                + "&auth%5Bdomain%5D=demo.bitrix24.com"
                + "&data%5Bbot%5D%5Bid%5D=456"
                + "&data%5Bbot%5D%5BisHidden%5D=0"
                + "&data%5Bbot%5D%5BisSupportOpenline%5D=0"
                + "&data%5Bbot%5D%5BisReactionsEnabled%5D=1"
                + "&data%5Bchat%5D%5Bid%5D=99"
                + "&data%5Bchat%5D%5BdialogId%5D=chat99"
                + "&data%5Bmessage%5D%5Bid%5D=888"
                + "&data%5Bmessage%5D%5BchatId%5D=99"
                + "&data%5Bmessage%5D%5BauthorId%5D=7"
                + "&data%5Bmessage%5D%5Bdate%5D=2025-01-15T10%3A30%3A00%2B03%3A00"
                + "&data%5Bmessage%5D%5Btext%5D=Hello+from+webhook"
                + "&data%5Bmessage%5D%5BisSystem%5D=0"
                + "&data%5Bmessage%5D%5BviewedByOthers%5D=1"
                + "&data%5Bmessage%5D%5BreplyId%5D="
                + "&data%5Buser%5D%5Bid%5D=7"
                + "&data%5Buser%5D%5Bactive%5D=1"
                + "&data%5Buser%5D%5Bname%5D=Alice"
                + "&data%5Buser%5D%5Bextranet%5D=0"
                + "&data%5Buser%5D%5Bbot%5D=0"
                + "&data%5Buser%5D%5Bconnector%5D=0"
                + "&data%5Buser%5D%5Bidle%5D=0"
                + "&data%5Buser%5D%5Babsent%5D=0"
                + "&data%5Buser%5D%5Bdepartments%5D%5B0%5D=1"
                + "&data%5Buser%5D%5Bdepartments%5D%5B1%5D=5";

        boolean dispatched = dispatcher.dispatchWebhookForm(formUrlEncoded);

        assertThat(dispatched).isTrue();
        assertThat(received).hasSize(1);
        Event event = received.get(0);
        assertThat(event.isMessage()).isTrue();
        assertThat(event.getMessageText()).isEqualTo("Hello from webhook");
        assertThat(event.getChatId()).isEqualTo("99");
        assertThat(event.getDialogId()).isEqualTo("chat99");
        assertThat(event.getUserId()).isEqualTo(7L);
        assertThat(event.getUserName()).isEqualTo("Alice");
        assertThat(event.getAuth().getApplicationToken()).isEqualTo("form_secret_token");
        assertThat(event.getAuth().getDomain()).isEqualTo("demo.bitrix24.com");

        assertThat(event.getData().getBot().getIsHidden()).isFalse();
        assertThat(event.getData().getBot().getIsSupportOpenline()).isFalse();
        assertThat(event.getData().getBot().getIsReactionsEnabled()).isTrue();
        assertThat(event.getData().getMessage().getIsSystem()).isFalse();
        assertThat(event.getData().getMessage().getViewedByOthers()).isTrue();
        assertThat(event.getData().getMessage().getReplyId()).isNull();
        assertThat(event.getData().getMessage().getDate()).isEqualTo(Instant.parse("2025-01-15T07:30:00Z"));
        assertThat(event.getData().getUser().getActive()).isTrue();
        assertThat(event.getData().getUser().getExtranet()).isFalse();
        assertThat(event.getData().getUser().getBot()).isFalse();
        assertThat(event.getData().getUser().getConnector()).isFalse();
        assertThat(event.getData().getUser().getIdle()).isFalse();
        assertThat(event.getData().getUser().getAbsent()).isFalse();
        assertThat(event.getData().getUser().getDepartments()).containsExactly(1L, 5L);
    }

    @Test
    @DisplayName("Protects caller when user listener throws uncaught exception and forwards to onError")
    void testListenerExceptionHandling() {
        AtomicReference<Throwable> capturedError = new AtomicReference<>();

        BotEventListener failingListener = new BotEventListener() {
            @Override
            public void onEvent(Event event) {
                throw new IllegalStateException("Simulated business error in listener");
            }

            @Override
            public void onError(Throwable error) {
                capturedError.set(error);
            }
        };

        BotEventDispatcher dispatcher = new BotEventDispatcher(BotConfig.builder().build(), failingListener);

        // Dispatch must not throw
        boolean dispatched = dispatcher.dispatch(Event.builder().type(EventType.ONIMBOTV2MESSAGEADD).build());

        assertThat(dispatched).isTrue();
        assertThat(capturedError.get())
                .isNotNull()
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Simulated business error in listener");
    }
}
