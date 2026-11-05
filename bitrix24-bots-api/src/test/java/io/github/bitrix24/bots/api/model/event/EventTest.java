package io.github.bitrix24.bots.api.model.event;

import io.github.bitrix24.bots.api.json.BitrixJsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EventTest {

    @Test
    @DisplayName("Tests event predicate methods")
    void testEventPredicates() {
        Event messageEvent = Event.builder().type(EventType.ONIMBOTV2MESSAGEADD).build();
        assertThat(messageEvent.isMessage()).isTrue();
        assertThat(messageEvent.isCommand()).isFalse();

        Event commandEvent = Event.builder().type(EventType.ONIMBOTV2COMMANDADD).build();
        assertThat(commandEvent.isCommand()).isTrue();
        assertThat(commandEvent.isMessage()).isFalse();

        Event joinEvent = Event.builder().type(EventType.ONIMBOTV2JOINCHAT).build();
        assertThat(joinEvent.isJoinChat()).isTrue();

        Event deleteEvent = Event.builder().type(EventType.ONIMBOTV2DELETE).build();
        assertThat(deleteEvent.isDelete()).isTrue();

        Event msgUpdate = Event.builder().type(EventType.ONIMBOTV2MESSAGEUPDATE).build();
        assertThat(msgUpdate.isMessageUpdate()).isTrue();

        Event msgDelete = Event.builder().type(EventType.ONIMBOTV2MESSAGEDELETE).build();
        assertThat(msgDelete.isMessageDelete()).isTrue();

        Event reaction = Event.builder().type(EventType.ONIMBOTV2REACTIONCHANGE).build();
        assertThat(reaction.isReactionChange()).isTrue();

        Event context = Event.builder().type(EventType.ONIMBOTV2CONTEXTGET).build();
        assertThat(context.isContextGet()).isTrue();
    }

    @Test
    @DisplayName("Tests accessor helpers with complete data")
    void testAccessorsWithData() {
        Event event = Event.builder()
                .eventId(12345L)
                .type(EventType.ONIMBOTV2MESSAGEADD)
                .date(Instant.parse("2025-01-01T00:00:00Z"))
                .data(EventData.builder()
                        .chat(ChatInfo.builder().id("100").dialogId("chat100").name("My Chat").build())
                        .message(MessageInfo.builder().id(200L).text("Ping message").build())
                        .user(UserInfo.builder().id(300L).name("John Doe").build())
                        .command(CommandInfo.builder().id(400L).command("/start").params("param1").build())
                        .bot(BotInfo.builder().id(500L).code("bot_code").build())
                        .auth(AuthInfo.builder().applicationToken("tok_123").domain("myportal.ru").build())
                        .build())
                .build();

        assertThat(event.getEventId()).isEqualTo(12345L);
        assertThat(event.getChatId()).isEqualTo("100");
        assertThat(event.getDialogId()).isEqualTo("chat100");
        assertThat(event.getMessageText()).isEqualTo("Ping message");
        assertThat(event.getUserId()).isEqualTo(300L);
        assertThat(event.getUserName()).isEqualTo("John Doe");
        assertThat(event.getCommandName()).isEqualTo("/start");
        assertThat(event.getCommandParams()).isEqualTo("param1");
        assertThat(event.getBotId()).isEqualTo(500L);
        assertThat(event.getAuth()).isNotNull();
        assertThat(event.getAuth().getApplicationToken()).isEqualTo("tok_123");
    }

    @Test
    @DisplayName("Tests ChatInfo numeric id parsing")
    void testChatInfoNumericId() {
        ChatInfo chatNum = ChatInfo.builder().id("55").build();
        assertThat(chatNum.getNumericId()).isEqualTo(55L);

        ChatInfo chatPrefixed = ChatInfo.builder().id("chat123").build();
        assertThat(chatPrefixed.getNumericId()).isEqualTo(123L);

        ChatInfo chatInvalid = ChatInfo.builder().id("invalid").build();
        assertThat(chatInvalid.getNumericId()).isNull();

        ChatInfo chatNull = ChatInfo.builder().id(null).build();
        assertThat(chatNull.getNumericId()).isNull();
    }

    @Test
    @DisplayName("Tests EventType fromString safety and JSON deserialization")
    void testEventTypeFromStringAndJson() {
        assertThat(EventType.fromString("ONIMBOTV2MESSAGEADD")).isEqualTo(EventType.ONIMBOTV2MESSAGEADD);
        assertThat(EventType.fromString("onimbotv2messageadd")).isEqualTo(EventType.ONIMBOTV2MESSAGEADD);
        assertThat(EventType.fromString("OnImBotV2CommandAdd")).isEqualTo(EventType.ONIMBOTV2COMMANDADD);
        assertThat(EventType.fromString("UNKNOWN_EVENT")).isEqualTo(EventType.UNKNOWN);
        assertThat(EventType.fromString(null)).isEqualTo(EventType.UNKNOWN);
        assertThat(EventType.fromString("")).isEqualTo(EventType.UNKNOWN);

        String json = """
                {
                    "eventId": 1001,
                    "type": "ONIMBOTV2MESSAGEADD",
                    "date": "2025-01-15T10:30:00+03:00",
                    "data": {
                        "message": {"id": 789, "text": "hello"},
                        "chat": {"id": "5", "dialogId": "chat5"}
                    }
                }
                """;
        Event parsed = BitrixJsonMapper.fromJson(json, Event.class);
        assertThat(parsed.getEventId()).isEqualTo(1001L);
        assertThat(parsed.getType()).isEqualTo(EventType.ONIMBOTV2MESSAGEADD);
        assertThat(parsed.isMessage()).isTrue();
        assertThat(parsed.getMessageText()).isEqualTo("hello");
        assertThat(parsed.getDialogId()).isEqualTo("chat5");
    }
}
