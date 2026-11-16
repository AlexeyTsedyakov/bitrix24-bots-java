package io.github.bitrix24.bots.api.model.event;

import io.github.bitrix24.bots.api.json.BitrixJsonMapper;
import io.github.bitrix24.bots.api.model.object.*;
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
                        .chat(ChatInfo.builder().id(100L).dialogId("chat100").name("My Chat").build())
                        .message(MessageInfo.builder().id(200L).text("Ping message").build())
                        .user(User.builder().id(300L).name("John Doe").build())
                        .command(CommandInfo.builder().id(400L).command("/start").params("param1").build())
                        .bot(BotInfo.builder().id(500L).code("bot_code").build())
                        .auth(AuthInfo.builder().applicationToken("tok_123").domain("myportal.ru").build())
                        .build())
                .build();

        assertThat(event.getEventId()).isEqualTo(12345L);
        assertThat(event.getChatId()).isEqualTo(100L);
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

    @Test
    @DisplayName("Tests object.User deserialization for API responses and event/webhook compound fields")
    void testUserDeserialization() {
        String apiUserJson = """
                {
                    "id": 1,
                    "active": true,
                    "name": "John Smith",
                    "firstName": "John",
                    "lastName": "Smith",
                    "workPosition": "Developer",
                    "color": "#df532d",
                    "avatar": "https://example.bitrix24.com/avatar.png",
                    "gender": "M",
                    "birthday": "15-03",
                    "extranet": false,
                    "bot": false,
                    "connector": false,
                    "externalAuthId": "default",
                    "status": "online",
                    "idle": "2025-01-15T10:00:00+03:00",
                    "lastActivityDate": "2025-01-15T14:25:00+03:00",
                    "mobileLastDate": false,
                    "desktopLastDate": "2025-01-15T14:25:00+03:00",
                    "absent": "2025-01-20T00:00:00+03:00",
                    "departments": [1, 5],
                    "phones": {
                        "work_phone": "+11234567890",
                        "personal_mobile": "+10987654321"
                    },
                    "type": "user",
                    "internalAccount": false,
                    "intranetUser": true,
                    "website": "https://example.com",
                    "email": "john@example.com"
                }
                """;

        User apiUser = BitrixJsonMapper.fromJson(apiUserJson, User.class);
        assertThat(apiUser).isNotNull();
        assertThat(apiUser.getId()).isEqualTo(1L);
        assertThat(apiUser.getActive()).isTrue();
        assertThat(apiUser.getName()).isEqualTo("John Smith");
        assertThat(apiUser.getFirstName()).isEqualTo("John");
        assertThat(apiUser.getLastName()).isEqualTo("Smith");
        assertThat(apiUser.getWorkPosition()).isEqualTo("Developer");
        assertThat(apiUser.getColor()).isEqualTo("#df532d");
        assertThat(apiUser.getAvatar()).isEqualTo("https://example.bitrix24.com/avatar.png");
        assertThat(apiUser.getGender()).isEqualTo("M");
        assertThat(apiUser.getBirthday()).isEqualTo("15-03");
        assertThat(apiUser.getExtranet()).isFalse();
        assertThat(apiUser.getBot()).isFalse();
        assertThat(apiUser.getConnector()).isFalse();
        assertThat(apiUser.getExternalAuthId()).isEqualTo("default");
        assertThat(apiUser.getStatus()).isEqualTo("online");
        assertThat(apiUser.getIdle()).isTrue();
        assertThat(apiUser.getLastActivityDate()).isEqualTo(Instant.parse("2025-01-15T11:25:00Z"));
        assertThat(apiUser.getMobileLastDate()).isNull();
        assertThat(apiUser.getDesktopLastDate()).isEqualTo(Instant.parse("2025-01-15T11:25:00Z"));
        assertThat(apiUser.getAbsent()).isTrue();
        assertThat(apiUser.getDepartments()).containsExactly(1L, 5L);
        assertThat(apiUser.getPhones()).isNotNull();
        assertThat(apiUser.getPhones().get("work_phone").asString()).isEqualTo("+11234567890");
        assertThat(apiUser.getPhones().get("personal_mobile").asString()).isEqualTo("+10987654321");
        assertThat(apiUser.getType()).isEqualTo("user");
        assertThat(apiUser.getInternalAccount()).isFalse();
        assertThat(apiUser.getIntranetUser()).isTrue();
        assertThat(apiUser.getWebsite()).isEqualTo("https://example.com");
        assertThat(apiUser.getEmail()).isEqualTo("john@example.com");

        String webhookUserJson = """
                {
                    "id": "7",
                    "active": "1",
                    "name": "Alice",
                    "idle": "0",
                    "lastActivityDate": "0",
                    "mobileLastDate": "0",
                    "desktopLastDate": "0",
                    "absent": "0",
                    "phones": false,
                    "internalAccount": "1",
                    "intranetUser": "0"
                }
                """;

        User webhookUser = BitrixJsonMapper.fromJson(webhookUserJson, User.class);
        assertThat(webhookUser).isNotNull();
        assertThat(webhookUser.getId()).isEqualTo(7L);
        assertThat(webhookUser.getActive()).isTrue();
        assertThat(webhookUser.getIdle()).isFalse();
        assertThat(webhookUser.getLastActivityDate()).isNull();
        assertThat(webhookUser.getMobileLastDate()).isNull();
        assertThat(webhookUser.getDesktopLastDate()).isNull();
        assertThat(webhookUser.getAbsent()).isFalse();
        assertThat(webhookUser.getPhones().isBoolean()).isTrue();
        assertThat(webhookUser.getPhones().asBoolean()).isFalse();
        assertThat(webhookUser.getInternalAccount()).isTrue();
        assertThat(webhookUser.getIntranetUser()).isFalse();
    }
}
