package io.github.bitrix24.bots.api.model.method.event;

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
                        .chat(Chat.builder().id(100L).dialogId("chat100").name("My Chat").build())
                        .message(Message.builder().id(200L).text("Ping message").build())
                        .user(User.builder().id(300L).name("John Doe").build())
                        .command(Command.builder().id("400").command("/start").params("param1").build())
                        .bot(Bot.builder().id(500L).code("bot_code").build())
                        .build())
                .auth(Auth.builder().applicationToken("tok_123").domain("myportal.ru").build())
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
        assertThat(EventType.fromString("ONIMBOTV2JOINCHAT")).isEqualTo(EventType.ONIMBOTV2JOINCHAT);
        assertThat(EventType.fromString("ONIMBOTV2DELETE")).isEqualTo(EventType.ONIMBOTV2DELETE);
        assertThat(EventType.fromString("ONIMBOTV2MESSAGEUPDATE")).isEqualTo(EventType.ONIMBOTV2MESSAGEUPDATE);
        assertThat(EventType.fromString("ONIMBOTV2MESSAGEDELETE")).isEqualTo(EventType.ONIMBOTV2MESSAGEDELETE);
        assertThat(EventType.fromString("ONIMBOTV2REACTIONCHANGE")).isEqualTo(EventType.ONIMBOTV2REACTIONCHANGE);
        assertThat(EventType.fromString("ONIMBOTV2CONTEXTGET")).isEqualTo(EventType.ONIMBOTV2CONTEXTGET);

        assertThat(EventType.fromString("ONIMV2MESSAGEADD")).isEqualTo(EventType.ONIMV2MESSAGEADD);
        assertThat(EventType.fromString("onimv2messageupdate")).isEqualTo(EventType.ONIMV2MESSAGEUPDATE);
        assertThat(EventType.fromString("ONIMV2MESSAGEDELETE")).isEqualTo(EventType.ONIMV2MESSAGEDELETE);
        assertThat(EventType.fromString("ONIMV2REACTIONCHANGE")).isEqualTo(EventType.ONIMV2REACTIONCHANGE);
        assertThat(EventType.fromString("ONIMV2JOINCHAT")).isEqualTo(EventType.ONIMV2JOINCHAT);

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

        String userEventJson = """
                {
                    "eventId": 1002,
                    "type": "ONIMV2MESSAGEADD",
                    "date": "2025-01-15T10:31:00+03:00",
                    "data": {
                        "message": {"id": 5012, "text": "user message"},
                        "chat": {"id": 5, "dialogId": "chat5"}
                    }
                }
                """;
        Event userEvent = BitrixJsonMapper.fromJson(userEventJson, Event.class);
        assertThat(userEvent.getEventId()).isEqualTo(1002L);
        assertThat(userEvent.getType()).isEqualTo(EventType.ONIMV2MESSAGEADD);
        assertThat(userEvent.getMessageText()).isEqualTo("user message");
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

    @Test
    @DisplayName("Tests object.Message deserialization for events and method responses")
    void testMessageDeserialization() {
        String methodMessageJson = """
                {
                    "id": 789,
                    "chatId": 5,
                    "authorId": 1,
                    "date": "2025-01-15T10:30:00+03:00",
                    "text": "Forwarded hello",
                    "isSystem": false,
                    "uuid": "uuid-123",
                    "forward": {
                        "id": "3/100",
                        "userId": 2,
                        "chatTitle": "Source Chat",
                        "chatType": "chat",
                        "parentChatId": 0,
                        "parentMessageId": 0,
                        "parentChatType": ""
                    },
                    "params": {
                        "REPLY_ID": "780",
                        "IS_EDITED": "Y"
                    },
                    "viewedByOthers": true,
                    "unread": false,
                    "viewed": true
                }
                """;

        Message methodMessage = BitrixJsonMapper.fromJson(methodMessageJson, Message.class);
        assertThat(methodMessage).isNotNull();
        assertThat(methodMessage.getId()).isEqualTo(789L);
        assertThat(methodMessage.getChatId()).isEqualTo(5L);
        assertThat(methodMessage.getAuthorId()).isEqualTo(1L);
        assertThat(methodMessage.getDate()).isEqualTo(Instant.parse("2025-01-15T07:30:00Z"));
        assertThat(methodMessage.getText()).isEqualTo("Forwarded hello");
        assertThat(methodMessage.getIsSystem()).isFalse();
        assertThat(methodMessage.getUuid()).isEqualTo("uuid-123");
        assertThat(methodMessage.getForward()).isNotNull();
        assertThat(methodMessage.getForward().get("id").asString()).isEqualTo("3/100");
        assertThat(methodMessage.getForward().get("userId").asLong()).isEqualTo(2L);
        assertThat(methodMessage.getParams()).isNotNull();
        assertThat(methodMessage.getParams().get("REPLY_ID").asString()).isEqualTo("780");
        assertThat(methodMessage.getViewedByOthers()).isTrue();
        assertThat(methodMessage.getUnread()).isFalse();
        assertThat(methodMessage.getViewed()).isTrue();

        String eventMessageJson = """
                {
                    "id": 790,
                    "chatId": 5,
                    "authorId": 0,
                    "date": "2025-01-15T10:35:00+03:00",
                    "text": "System message",
                    "isSystem": true,
                    "uuid": null,
                    "forward": null,
                    "params": [],
                    "viewedByOthers": false
                }
                """;

        Message eventMessage = BitrixJsonMapper.fromJson(eventMessageJson, Message.class);
        assertThat(eventMessage).isNotNull();
        assertThat(eventMessage.getId()).isEqualTo(790L);
        assertThat(eventMessage.getAuthorId()).isEqualTo(0L);
        assertThat(eventMessage.getIsSystem()).isTrue();
        assertThat(eventMessage.getUuid()).isNull();
        assertThat(eventMessage.getForward().isNull()).isTrue();
        assertThat(eventMessage.getParams().isArray()).isTrue();
        assertThat(eventMessage.getParams()).isEmpty();
        assertThat(eventMessage.getViewedByOthers()).isFalse();
        assertThat(eventMessage.getUnread()).isNull();
        assertThat(eventMessage.getViewed()).isNull();
    }

    @Test
    @DisplayName("Tests object.File deserialization for document and media files")
    void testFileDeserialization() {
        String documentFileJson = """
                {
                    "id": 138,
                    "chatId": 5,
                    "date": "2025-01-15T10:30:00+03:00",
                    "type": "file",
                    "name": "report.pdf",
                    "extension": "pdf",
                    "size": 35341,
                    "duration": null,
                    "image": false,
                    "authorId": 1,
                    "authorName": "John Smith",
                    "isTranscribable": false,
                    "isVideoNote": false,
                    "isVoiceNote": false
                }
                """;

        File docFile = BitrixJsonMapper.fromJson(documentFileJson, File.class);
        assertThat(docFile).isNotNull();
        assertThat(docFile.getId()).isEqualTo(138L);
        assertThat(docFile.getChatId()).isEqualTo(5L);
        assertThat(docFile.getDate()).isEqualTo(Instant.parse("2025-01-15T07:30:00Z"));
        assertThat(docFile.getType()).isEqualTo("file");
        assertThat(docFile.getName()).isEqualTo("report.pdf");
        assertThat(docFile.getExtension()).isEqualTo("pdf");
        assertThat(docFile.getSize()).isEqualTo(35341L);
        assertThat(docFile.getDuration()).isNull();
        assertThat(docFile.getImage()).isNotNull();
        assertThat(docFile.getImage().isBoolean()).isTrue();
        assertThat(docFile.getImage().asBoolean()).isFalse();
        assertThat(docFile.getAuthorId()).isEqualTo(1L);
        assertThat(docFile.getAuthorName()).isEqualTo("John Smith");
        assertThat(docFile.getIsTranscribable()).isFalse();
        assertThat(docFile.getIsVideoNote()).isFalse();
        assertThat(docFile.getIsVoiceNote()).isFalse();

        String mediaFileJson = """
                {
                    "id": 139,
                    "chatId": 5,
                    "date": "2025-01-15T10:35:00+03:00",
                    "type": "video",
                    "name": "clip.mp4",
                    "extension": "mp4",
                    "size": 1024000,
                    "duration": 42,
                    "image": {
                        "height": 600,
                        "width": 800
                    },
                    "authorId": 2,
                    "authorName": "Jane Doe",
                    "isTranscribable": true,
                    "isVideoNote": true,
                    "isVoiceNote": false
                }
                """;

        File mediaFile = BitrixJsonMapper.fromJson(mediaFileJson, File.class);
        assertThat(mediaFile).isNotNull();
        assertThat(mediaFile.getId()).isEqualTo(139L);
        assertThat(mediaFile.getType()).isEqualTo("video");
        assertThat(mediaFile.getDuration()).isEqualTo(42);
        assertThat(mediaFile.getImage().isObject()).isTrue();
        assertThat(mediaFile.getImage().get("height").asInt()).isEqualTo(600);
        assertThat(mediaFile.getImage().get("width").asInt()).isEqualTo(800);
        assertThat(mediaFile.getIsTranscribable()).isTrue();
        assertThat(mediaFile.getIsVideoNote()).isTrue();
        assertThat(mediaFile.getIsVoiceNote()).isFalse();
    }

    @Test
    @DisplayName("Tests object.Command deserialization across register/update, list, and event contexts")
    void testCommandDeserialization() {
        String registerCommandJson = """
                {
                    "id": 78,
                    "botId": 456,
                    "command": "/help",
                    "common": false,
                    "hidden": false,
                    "extranetSupport": true
                }
                """;

        Command registerCommand = BitrixJsonMapper.fromJson(registerCommandJson, Command.class);
        assertThat(registerCommand).isNotNull();
        assertThat(registerCommand.getId()).isEqualTo("78");
        assertThat(registerCommand.getBotId()).isEqualTo(456L);
        assertThat(registerCommand.getCommand()).isEqualTo("/help");
        assertThat(registerCommand.getCommon()).isFalse();
        assertThat(registerCommand.getHidden()).isFalse();
        assertThat(registerCommand.getExtranetSupport()).isTrue();

        String listCommandJson = """
                {
                    "id": 78,
                    "botId": 456,
                    "command": "/help",
                    "category": "Support Bot",
                    "common": "N",
                    "context": "",
                    "title": "Показать помощь",
                    "params": "запрос",
                    "extranet": "Y"
                }
                """;

        Command listCommand = BitrixJsonMapper.fromJson(listCommandJson, Command.class);
        assertThat(listCommand).isNotNull();
        assertThat(listCommand.getId()).isEqualTo("78");
        assertThat(listCommand.getBotId()).isEqualTo(456L);
        assertThat(listCommand.getCommand()).isEqualTo("/help");
        assertThat(listCommand.getCategory()).isEqualTo("Support Bot");
        assertThat(listCommand.getCommon()).isFalse();
        assertThat(listCommand.getContext()).isEmpty();
        assertThat(listCommand.getTitle()).isEqualTo("Показать помощь");
        assertThat(listCommand.getParams()).isEqualTo("запрос");
        assertThat(listCommand.getExtranet()).isTrue();

        String builtInCommandJson = """
                {
                    "id": "def0",
                    "botId": 0,
                    "command": "/me",
                    "category": "Bitrix24",
                    "common": "Y",
                    "context": "",
                    "title": "Action",
                    "params": "text",
                    "extranet": "Y"
                }
                """;

        Command builtInCommand = BitrixJsonMapper.fromJson(builtInCommandJson, Command.class);
        assertThat(builtInCommand).isNotNull();
        assertThat(builtInCommand.getId()).isEqualTo("def0");
        assertThat(builtInCommand.getBotId()).isEqualTo(0L);
        assertThat(builtInCommand.getCommand()).isEqualTo("/me");
        assertThat(builtInCommand.getCommon()).isTrue();

        String eventCommandJson = """
                {
                    "id": 78,
                    "command": "/help",
                    "params": "topic",
                    "context": "textarea"
                }
                """;

        Command eventCommand = BitrixJsonMapper.fromJson(eventCommandJson, Command.class);
        assertThat(eventCommand).isNotNull();
        assertThat(eventCommand.getId()).isEqualTo("78");
        assertThat(eventCommand.getCommand()).isEqualTo("/help");
        assertThat(eventCommand.getParams()).isEqualTo("topic");
        assertThat(eventCommand.getContext()).isEqualTo("textarea");
    }

    @Test
    @DisplayName("Tests object.Chat deserialization for method responses and webhook events")
    void testChatDeserialization() {
        String methodChatJson = """
                {
                    "id": 5,
                    "dialogId": "chat5",
                    "name": "Support Chat",
                    "type": "chat",
                    "messageType": "C",
                    "owner": 456,
                    "color": "#4ba984",
                    "avatar": "",
                    "description": "Help desk",
                    "extranet": false,
                    "role": "owner",
                    "containsCollaber": false,
                    "muteList": [456],
                    "entityType": "LINES",
                    "entityId": "line1",
                    "entityData1": "d1",
                    "entityData2": "d2",
                    "entityData3": "d3",
                    "entityLink": {
                        "type": "LINES",
                        "url": "/online/1",
                        "id": "1"
                    },
                    "diskFolderId": 42,
                    "permissions": {
                        "manageUsersAdd": "member",
                        "manageSettings": "owner",
                        "canPost": "member"
                    },
                    "parentChatId": 3,
                    "parentMessageId": 100,
                    "isNew": false,
                    "textFieldEnabled": true,
                    "backgroundId": "azure",
                    "hasManageCapability": true,
                    "canHaveThreads": true,
                    "dateCreate": "2025-01-15T10:00:00+03:00",
                    "lastMessageId": 789,
                    "lastId": 789,
                    "managerList": [456, 1],
                    "messageCount": 15,
                    "userCounter": 3,
                    "unreadId": 0,
                    "lastMessageViews": {
                        "messageId": 789,
                        "firstViewers": [],
                        "countOfViewers": 0
                    },
                    "markedId": 780,
                    "public": {
                        "code": "abc",
                        "link": "https://example.bitrix24.ru/online/abc"
                    },
                    "guestCount": 0
                }
                """;

        Chat methodChat = BitrixJsonMapper.fromJson(methodChatJson, Chat.class);
        assertThat(methodChat).isNotNull();
        assertThat(methodChat.getId()).isEqualTo(5L);
        assertThat(methodChat.getDialogId()).isEqualTo("chat5");
        assertThat(methodChat.getName()).isEqualTo("Support Chat");
        assertThat(methodChat.getType()).isEqualTo("chat");
        assertThat(methodChat.getMessageType()).isEqualTo("C");
        assertThat(methodChat.getOwner()).isEqualTo(456L);
        assertThat(methodChat.getColor()).isEqualTo("#4ba984");
        assertThat(methodChat.getDescription()).isEqualTo("Help desk");
        assertThat(methodChat.getExtranet()).isFalse();
        assertThat(methodChat.getRole()).isEqualTo("owner");
        assertThat(methodChat.getContainsCollaber()).isFalse();
        assertThat(methodChat.getMuteList()).containsExactly(456L);
        assertThat(methodChat.getEntityType()).isEqualTo("LINES");
        assertThat(methodChat.getEntityLink().get("type").asString()).isEqualTo("LINES");
        assertThat(methodChat.getDiskFolderId()).isEqualTo(42L);
        assertThat(methodChat.getPermissions().get("manageSettings").asString()).isEqualTo("owner");
        assertThat(methodChat.getParentChatId()).isEqualTo(3L);
        assertThat(methodChat.getParentMessageId()).isEqualTo(100L);
        assertThat(methodChat.getIsNew()).isFalse();
        assertThat(methodChat.getTextFieldEnabled()).isTrue();
        assertThat(methodChat.getBackgroundId()).isEqualTo("azure");
        assertThat(methodChat.getHasManageCapability()).isTrue();
        assertThat(methodChat.getCanHaveThreads()).isTrue();
        assertThat(methodChat.getDateCreate()).isEqualTo(Instant.parse("2025-01-15T07:00:00Z"));
        assertThat(methodChat.getLastMessageId()).isEqualTo(789L);
        assertThat(methodChat.getLastId()).isEqualTo(789L);
        assertThat(methodChat.getManagerList()).containsExactly(456L, 1L);
        assertThat(methodChat.getMessageCount()).isEqualTo(15);
        assertThat(methodChat.getUserCounter()).isEqualTo(3);
        assertThat(methodChat.getUnreadId()).isEqualTo(0L);
        assertThat(methodChat.getLastMessageViews().get("messageId").asLong()).isEqualTo(789L);
        assertThat(methodChat.getMarkedId()).isEqualTo(780L);
        assertThat(methodChat.getPublicSettings().get("code").asString()).isEqualTo("abc");
        assertThat(methodChat.getGuestCount()).isEqualTo(0);

        String webhookChatJson = """
                {
                    "id": "5",
                    "dialogId": "chat5",
                    "textFieldEnabled": "1",
                    "hasManageCapability": "0",
                    "canHaveThreads": "1",
                    "public": ""
                }
                """;

        Chat webhookChat = BitrixJsonMapper.fromJson(webhookChatJson, Chat.class);
        assertThat(webhookChat).isNotNull();
        assertThat(webhookChat.getId()).isEqualTo(5L);
        assertThat(webhookChat.getTextFieldEnabled()).isTrue();
        assertThat(webhookChat.getHasManageCapability()).isFalse();
        assertThat(webhookChat.getCanHaveThreads()).isTrue();
        assertThat(webhookChat.getPublicSettings().isString()).isTrue();
        assertThat(webhookChat.getPublicSettings().asString()).isEmpty();
    }

    @Test
    @DisplayName("Tests event.Auth deserialization for webhook auth and data.bot.auth payloads")
    void testAuthDeserialization() {
        String webhookAuthJson = """
                {
                    "access_token": "0b02a0690000071b0008440001b0007a16b39202c2490f015",
                    "expires_in": "3600",
                    "scope": "imbot",
                    "domain": "some-domain.bitrix24.ru",
                    "server_endpoint": "https://oauth.bitrix24.tech/rest/",
                    "status": "F",
                    "client_endpoint": "https://some-domain.bitrix24.ru/rest/",
                    "member_id": "bac1cd5c8940947a75e0d71b1a84e348",
                    "refresh_token": "refresh_tok_456",
                    "application_token": "831c76b092f9f135d9b6b36c3a720757"
                }
                """;

        Auth auth = BitrixJsonMapper.fromJson(webhookAuthJson, Auth.class);
        assertThat(auth).isNotNull();
        assertThat(auth.getAccessToken()).isEqualTo("0b02a0690000071b0008440001b0007a16b39202c2490f015");
        assertThat(auth.getExpiresIn()).isEqualTo(3600L);
        assertThat(auth.getScope()).isEqualTo("imbot");
        assertThat(auth.getDomain()).isEqualTo("some-domain.bitrix24.ru");
        assertThat(auth.getServerEndpoint()).isEqualTo("https://oauth.bitrix24.tech/rest/");
        assertThat(auth.getStatus()).isEqualTo("F");
        assertThat(auth.getClientEndpoint()).isEqualTo("https://some-domain.bitrix24.ru/rest/");
        assertThat(auth.getMemberId()).isEqualTo("bac1cd5c8940947a75e0d71b1a84e348");
        assertThat(auth.getRefreshToken()).isEqualTo("refresh_tok_456");
        assertThat(auth.getApplicationToken()).isEqualTo("831c76b092f9f135d9b6b36c3a720757");
        assertThat(auth.toString())
                .doesNotContain("0b02a0690000071b0008440001b0007a16b39202c2490f015")
                .doesNotContain("refresh_tok_456")
                .doesNotContain("831c76b092f9f135d9b6b36c3a720757");

        String serialized = BitrixJsonMapper.toJson(auth);
        assertThat(serialized)
                .contains("\"access_token\":\"0b02a0690000071b0008440001b0007a16b39202c2490f015\"")
                .contains("\"expires_in\":3600")
                .contains("\"application_token\":\"831c76b092f9f135d9b6b36c3a720757\"");
    }

    @Test
    @DisplayName("Tests object.Bot deserialization for extended, brief, and webhook formats")
    void testBotDeserialization() {
        String extendedBotJson = """
                {
                    "id": 456,
                    "code": "support_bot",
                    "type": "bot",
                    "isHidden": false,
                    "isSupportOpenline": true,
                    "isReactionsEnabled": true,
                    "backgroundId": null,
                    "language": "ru",
                    "moduleId": "rest",
                    "eventMode": "fetch",
                    "countMessage": 150,
                    "countCommand": 3,
                    "countChat": 12,
                    "countUser": 45
                }
                """;

        Bot extendedBot = BitrixJsonMapper.fromJson(extendedBotJson, Bot.class);
        assertThat(extendedBot).isNotNull();
        assertThat(extendedBot.getId()).isEqualTo(456L);
        assertThat(extendedBot.getCode()).isEqualTo("support_bot");
        assertThat(extendedBot.getType()).isEqualTo("bot");
        assertThat(extendedBot.getIsHidden()).isFalse();
        assertThat(extendedBot.getIsSupportOpenline()).isTrue();
        assertThat(extendedBot.getIsReactionsEnabled()).isTrue();
        assertThat(extendedBot.getBackgroundId()).isNull();
        assertThat(extendedBot.getLanguage()).isEqualTo("ru");
        assertThat(extendedBot.getModuleId()).isEqualTo("rest");
        assertThat(extendedBot.getEventMode()).isEqualTo("fetch");
        assertThat(extendedBot.getCountMessage()).isEqualTo(150);
        assertThat(extendedBot.getCountCommand()).isEqualTo(3);
        assertThat(extendedBot.getCountChat()).isEqualTo(12);
        assertThat(extendedBot.getCountUser()).isEqualTo(45);
        assertThat(extendedBot.getAuth()).isNull();

        String briefBotJson = """
                {
                    "id": 457,
                    "code": "public_bot",
                    "type": "openline",
                    "isHidden": true,
                    "isSupportOpenline": false,
                    "isReactionsEnabled": false,
                    "backgroundId": "azure",
                    "language": "en"
                }
                """;

        Bot briefBot = BitrixJsonMapper.fromJson(briefBotJson, Bot.class);
        assertThat(briefBot).isNotNull();
        assertThat(briefBot.getId()).isEqualTo(457L);
        assertThat(briefBot.getCode()).isEqualTo("public_bot");
        assertThat(briefBot.getType()).isEqualTo("openline");
        assertThat(briefBot.getIsHidden()).isTrue();
        assertThat(briefBot.getIsSupportOpenline()).isFalse();
        assertThat(briefBot.getIsReactionsEnabled()).isFalse();
        assertThat(briefBot.getBackgroundId()).isEqualTo("azure");
        assertThat(briefBot.getLanguage()).isEqualTo("en");
        assertThat(briefBot.getModuleId()).isNull();
        assertThat(briefBot.getEventMode()).isNull();

        String webhookBotJson = """
                {
                    "id": "456",
                    "code": "support_bot",
                    "auth": {
                        "access_token": "tok_bot",
                        "expires_in": "3600",
                        "scope": "imbot",
                        "domain": "some-domain.bitrix24.ru",
                        "server_endpoint": "https://oauth.bitrix24.tech/rest/",
                        "status": "F",
                        "client_endpoint": "https://some-domain.bitrix24.ru/rest/",
                        "member_id": "bac1cd5c8940947a75e0d71b1a84e348",
                        "application_token": "app_tok"
                    }
                }
                """;

        Bot webhookBot = BitrixJsonMapper.fromJson(webhookBotJson, Bot.class);
        assertThat(webhookBot).isNotNull();
        assertThat(webhookBot.getId()).isEqualTo(456L);
        assertThat(webhookBot.getCode()).isEqualTo("support_bot");
        assertThat(webhookBot.getAuth()).isNotNull();
        assertThat(webhookBot.getAuth().getAccessToken()).isEqualTo("tok_bot");
        assertThat(webhookBot.getAuth().getExpiresIn()).isEqualTo(3600L);
        assertThat(webhookBot.getAuth().getApplicationToken()).isEqualTo("app_tok");
    }

    @Test
    @DisplayName("Tests method.event.EventData deserialization across imbot.v2 and im.v2 event payloads")
    void testEventDataDeserialization() {
        String messageReplyDataJson = """
                {
                    "bot": {
                        "id": 456,
                        "code": "support_bot",
                        "type": "bot",
                        "eventMode": "fetch"
                    },
                    "message": {
                        "id": 790,
                        "chatId": 5,
                        "authorId": 1,
                        "text": "Reply text"
                    },
                    "chat": {
                        "id": 5,
                        "dialogId": "chat5",
                        "type": "chat"
                    },
                    "user": {
                        "id": 1,
                        "name": "John Smith"
                    },
                    "language": "en",
                    "additionalMessages": [
                        {
                            "id": 789,
                            "chatId": 5,
                            "authorId": 456,
                            "text": "Original message"
                        }
                    ]
                }
                """;

        EventData messageData = BitrixJsonMapper.fromJson(messageReplyDataJson, EventData.class);
        assertThat(messageData).isNotNull();
        assertThat(messageData.getBot().getId()).isEqualTo(456L);
        assertThat(messageData.getMessage().getId()).isEqualTo(790L);
        assertThat(messageData.getChat().getDialogId()).isEqualTo("chat5");
        assertThat(messageData.getUser().getId()).isEqualTo(1L);
        assertThat(messageData.getLanguage()).isEqualTo("en");
        assertThat(messageData.getAdditionalMessages()).hasSize(1);
        assertThat(messageData.getAdditionalMessages().get(0).getId()).isEqualTo(789L);
        assertThat(messageData.getAdditionalMessages().get(0).getText()).isEqualTo("Original message");

        String contextDataJson = """
                {
                    "bot": {"id": 456, "code": "support_bot"},
                    "dialogId": "chat5",
                    "context": {"entityId": 164, "entityType": "task", "source": "link"},
                    "chat": {"id": 5, "dialogId": "chat5"},
                    "user": {"id": 1, "name": "John Smith"},
                    "language": "ru"
                }
                """;

        EventData contextData = BitrixJsonMapper.fromJson(contextDataJson, EventData.class);
        assertThat(contextData).isNotNull();
        assertThat(contextData.getDialogId()).isEqualTo("chat5");
        assertThat(contextData.getContext()).isNotNull();
        assertThat(contextData.getContext().get("entityId").asInt()).isEqualTo(164);
        assertThat(contextData.getContext().get("entityType").asString()).isEqualTo("task");
        assertThat(contextData.getLanguage()).isEqualTo("ru");

        String deleteAndReactionJson = """
                {
                    "bot": {"id": "456", "code": "support_bot"},
                    "messageId": "789",
                    "reaction": "like",
                    "action": "add",
                    "command": {"id": 78, "command": "/help", "params": "topic", "context": "textarea"},
                    "language": "en",
                    "additionalMessages": []
                }
                """;

        EventData deleteAndReactionData = BitrixJsonMapper.fromJson(deleteAndReactionJson, EventData.class);
        assertThat(deleteAndReactionData).isNotNull();
        assertThat(deleteAndReactionData.getBot().getId()).isEqualTo(456L);
        assertThat(deleteAndReactionData.getMessageId()).isEqualTo(789L);
        assertThat(deleteAndReactionData.getReaction()).isEqualTo("like");
        assertThat(deleteAndReactionData.getAction()).isEqualTo("add");
        assertThat(deleteAndReactionData.getCommand().getCommand()).isEqualTo("/help");
        assertThat(deleteAndReactionData.getAdditionalMessages()).isEmpty();
    }

    @Test
    @DisplayName("Tests method.event.Event deserialization for polling and webhook formats")
    void testEventPollingAndWebhookDeserialization() {
        String pollingEventJson = """
                {
                    "eventId": 1001,
                    "type": "ONIMBOTV2MESSAGEADD",
                    "date": "2024-10-11T09:58:12+03:00",
                    "data": {
                        "bot": {
                            "id": 456,
                            "code": "support_bot",
                            "type": "bot",
                            "eventMode": "fetch"
                        },
                        "message": {
                            "id": 193,
                            "chatId": 112,
                            "authorId": 129,
                            "text": "Привет!"
                        },
                        "chat": {
                            "id": 112,
                            "dialogId": "129",
                            "type": "private"
                        },
                        "user": {
                            "id": 129,
                            "name": "Мария Иванова"
                        },
                        "language": "ru",
                        "additionalMessages": []
                    }
                }
                """;

        Event pollingEvent = BitrixJsonMapper.fromJson(pollingEventJson, Event.class);
        assertThat(pollingEvent).isNotNull();
        assertThat(pollingEvent.getEventId()).isEqualTo(1001L);
        assertThat(pollingEvent.getType()).isEqualTo(EventType.ONIMBOTV2MESSAGEADD);
        assertThat(pollingEvent.getDate()).isEqualTo(Instant.parse("2024-10-11T06:58:12Z"));
        assertThat(pollingEvent.getEventHandlerId()).isNull();
        assertThat(pollingEvent.getBotId()).isEqualTo(456L);
        assertThat(pollingEvent.getChatId()).isEqualTo(112L);
        assertThat(pollingEvent.getDialogId()).isEqualTo("129");
        assertThat(pollingEvent.getMessageText()).isEqualTo("Привет!");

        String webhookEventJson = """
                {
                    "event": "ONIMBOTV2MESSAGEADD",
                    "event_handler_id": "1379",
                    "data": {
                        "bot": {
                            "id": "253",
                            "code": "fp_service_manager_bot"
                        },
                        "message": {
                            "id": "161798",
                            "chatId": "15324",
                            "authorId": "14",
                            "date": "2026-11-03T12:53:43+03:00",
                            "text": "hello world",
                            "isSystem": "0",
                            "viewedByOthers": "0"
                        },
                        "chat": {
                            "id": "15324",
                            "dialogId": "14",
                            "type": "private",
                            "extranet": "0",
                            "textFieldEnabled": "1"
                        },
                        "user": {
                            "id": "14",
                            "active": "1",
                            "name": "Иван Иванов",
                            "departments": ["13", "26"]
                        },
                        "language": "ru"
                    },
                    "ts": "1793699623",
                    "auth": {
                        "domain": "b24.example.ru",
                        "client_endpoint": "https://b24.example.ru/rest/",
                        "server_endpoint": "https://oauth.bitrix24.tech/rest/",
                        "member_id": "aa440860a0004183762656de0dcdc2eb",
                        "application_token": "top_app_token"
                    }
                }
                """;

        Event webhookEvent = BitrixJsonMapper.fromJson(webhookEventJson, Event.class);
        assertThat(webhookEvent).isNotNull();
        assertThat(webhookEvent.getEventId()).isNull();
        assertThat(webhookEvent.getType()).isEqualTo(EventType.ONIMBOTV2MESSAGEADD);
        assertThat(webhookEvent.isMessage()).isTrue();
        assertThat(webhookEvent.getEventHandlerId()).isEqualTo("1379");
        assertThat(webhookEvent.getDate()).isEqualTo(Instant.ofEpochSecond(1793699623L));
        assertThat(webhookEvent.getBotId()).isEqualTo(253L);
        assertThat(webhookEvent.getChatId()).isEqualTo(15324L);
        assertThat(webhookEvent.getDialogId()).isEqualTo("14");
        assertThat(webhookEvent.getMessageText()).isEqualTo("hello world");
        assertThat(webhookEvent.getAuth()).isNotNull();
        assertThat(webhookEvent.getAuth().getDomain()).isEqualTo("b24.example.ru");
        assertThat(webhookEvent.getAuth().getClientEndpoint()).isEqualTo("https://b24.example.ru/rest/");
        assertThat(webhookEvent.getAuth().getServerEndpoint()).isEqualTo("https://oauth.bitrix24.tech/rest/");
        assertThat(webhookEvent.getAuth().getMemberId()).isEqualTo("aa440860a0004183762656de0dcdc2eb");
        assertThat(webhookEvent.getAuth().getApplicationToken()).isEqualTo("top_app_token");

        String webhookBotAuthFallbackJson = """
                {
                    "event": "ONIMBOTV2DELETE",
                    "event_handler_id": "1335",
                    "ts": "1772093963",
                    "data": {
                        "bot": {
                            "id": "456",
                            "code": "support_bot",
                            "auth": {
                                "access_token": "tok_access",
                                "refresh_token": "tok_refresh",
                                "application_token": "tok_app"
                            }
                        }
                    }
                }
                """;

        Event botAuthFallbackEvent = BitrixJsonMapper.fromJson(webhookBotAuthFallbackJson, Event.class);
        assertThat(botAuthFallbackEvent).isNotNull();
        assertThat(botAuthFallbackEvent.isDelete()).isTrue();
        assertThat(botAuthFallbackEvent.getAuth()).isNotNull();
        assertThat(botAuthFallbackEvent.getAuth().getAccessToken()).isEqualTo("tok_access");
        assertThat(botAuthFallbackEvent.getAuth().getApplicationToken()).isEqualTo("tok_app");
    }
}
