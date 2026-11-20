package io.github.bitrix24.bots.api.model.method.command;

import io.github.bitrix24.bots.api.json.BitrixJsonMapper;
import io.github.bitrix24.bots.api.model.method.message.attach.Attach;
import io.github.bitrix24.bots.api.model.method.message.keyboard.Keyboard;
import io.github.bitrix24.bots.api.model.method.message.keyboard.KeyboardButton;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CommandModelTest {

    @Test
    @DisplayName("Tests method.command.CommandRegisterFields serialization, deserialization, and factory method")
    void testCommandRegisterFieldsSerialization() {
        CommandRegisterFields fields = CommandRegisterFields.builder()
                .command("help")
                .title(Map.of("en", "Show help", "ru", "Показать помощь"))
                .params(Map.of("en", "query", "ru", "запрос"))
                .common(true)
                .hidden(false)
                .extranetSupport(false)
                .build();

        String json = BitrixJsonMapper.toJson(fields);
        assertThat(json)
                .contains("\"command\":\"help\"")
                .contains("\"title\":{")
                .contains("\"en\":\"Show help\"")
                .contains("\"ru\":\"Показать помощь\"")
                .contains("\"params\":{")
                .contains("\"common\":true")
                .contains("\"hidden\":false")
                .contains("\"extranetSupport\":false");

        CommandRegisterFields deserialized = BitrixJsonMapper.fromJson(json, CommandRegisterFields.class);
        assertThat(deserialized).isEqualTo(fields);

        CommandRegisterFields factoryFields = CommandRegisterFields.of("ping", "Ping");
        assertThat(factoryFields.getCommand()).isEqualTo("ping");
        assertThat(factoryFields.getTitle())
                .containsEntry("en", "Ping")
                .containsEntry("ru", "Ping");
        assertThat(factoryFields.getParams()).isNull();
    }

    @Test
    @DisplayName("Tests method.command.CommandRegisterRequest serialization, factory method, and botToken masking")
    void testCommandRegisterRequestSerialization() {
        CommandRegisterFields fields = CommandRegisterFields.builder()
                .command("help")
                .title(Map.of("en", "Show help", "ru", "Показать помощь"))
                .params(Map.of("en", "query", "ru", "запрос"))
                .build();

        CommandRegisterRequest request = CommandRegisterRequest.builder()
                .botId(456L)
                .botToken("secret_bot_token")
                .fields(fields)
                .build();

        assertThat(request.toString()).doesNotContain("secret_bot_token");

        String json = BitrixJsonMapper.toJson(request);
        assertThat(json)
                .contains("\"botId\":456")
                .contains("\"botToken\":\"secret_bot_token\"")
                .contains("\"fields\":{")
                .contains("\"command\":\"help\"");

        CommandRegisterRequest deserialized = BitrixJsonMapper.fromJson(json, CommandRegisterRequest.class);
        assertThat(deserialized).isEqualTo(request);

        CommandRegisterRequest factoryRequest = CommandRegisterRequest.of(fields);
        assertThat(factoryRequest.getFields()).isEqualTo(fields);
        assertThat(factoryRequest.getBotId()).isNull();
        assertThat(factoryRequest.getBotToken()).isNull();
    }

    @Test
    @DisplayName("Tests method.command.CommandRegisterResult deserialization against imbot.v2.Command.register example")
    void testCommandRegisterResultDeserialization() {
        String json = """
                {
                    "command": {
                        "id": 42,
                        "botId": 456,
                        "command": "/help",
                        "common": false,
                        "hidden": false,
                        "extranetSupport": false
                    }
                }
                """;

        CommandRegisterResult result = BitrixJsonMapper.fromJson(json, CommandRegisterResult.class);
        assertThat(result).isNotNull();
        assertThat(result.getCommand()).isNotNull();
        assertThat(result.getCommand().getId()).isEqualTo("42");
        assertThat(result.getCommand().getBotId()).isEqualTo(456L);
        assertThat(result.getCommand().getCommand()).isEqualTo("/help");
        assertThat(result.getCommand().getCommon()).isFalse();
        assertThat(result.getCommand().getHidden()).isFalse();
        assertThat(result.getCommand().getExtranetSupport()).isFalse();
    }

    @Test
    @DisplayName("Tests method.command.CommandUnregisterRequest serialization, factory method, and botToken masking")
    void testCommandUnregisterRequestSerialization() {
        CommandUnregisterRequest request = CommandUnregisterRequest.builder()
                .botId(456L)
                .botToken("secret_bot_token")
                .commandId(42L)
                .build();

        assertThat(request.toString()).doesNotContain("secret_bot_token");

        String json = BitrixJsonMapper.toJson(request);
        assertThat(json)
                .contains("\"botId\":456")
                .contains("\"botToken\":\"secret_bot_token\"")
                .contains("\"commandId\":42");

        CommandUnregisterRequest deserialized = BitrixJsonMapper.fromJson(json, CommandUnregisterRequest.class);
        assertThat(deserialized).isEqualTo(request);

        CommandUnregisterRequest factoryRequest = CommandUnregisterRequest.of(42L);
        assertThat(factoryRequest.getCommandId()).isEqualTo(42L);
        assertThat(factoryRequest.getBotId()).isNull();
        assertThat(factoryRequest.getBotToken()).isNull();
    }

    @Test
    @DisplayName("Tests method.command.CommandAnswerFields serialization, deserialization, empty keyboard filter, and factory method")
    void testCommandAnswerFieldsSerialization() {
        Attach attach = Attach.builder()
                .colorToken("primary")
                .build()
                .addMessage("Command output");

        Keyboard keyboard = Keyboard.of(
                KeyboardButton.command("Help", "/help")
        );

        CommandAnswerFields fields = CommandAnswerFields.builder()
                .message("Here is the help text...")
                .attach(attach)
                .keyboard(keyboard)
                .system(false)
                .urlPreview(true)
                .build();

        String json = BitrixJsonMapper.toJson(fields);
        assertThat(json)
                .contains("\"message\":\"Here is the help text...\"")
                .contains("\"attach\":{")
                .contains("\"COLOR_TOKEN\":\"primary\"")
                .contains("\"keyboard\":{")
                .contains("\"BUTTONS\":[")
                .contains("\"COMMAND\":\"/help\"")
                .contains("\"system\":false")
                .contains("\"urlPreview\":true");

        CommandAnswerFields deserialized = BitrixJsonMapper.fromJson(json, CommandAnswerFields.class);
        assertThat(deserialized).isEqualTo(fields);

        CommandAnswerFields withEmptyKeyboard = CommandAnswerFields.builder()
                .message("No buttons")
                .keyboard(new Keyboard())
                .build();
        assertThat(BitrixJsonMapper.toJson(withEmptyKeyboard))
                .contains("\"message\":\"No buttons\"")
                .doesNotContain("\"keyboard\"");

        CommandAnswerFields factoryFields = CommandAnswerFields.of("Quick answer");
        assertThat(factoryFields.getMessage()).isEqualTo("Quick answer");
        assertThat(factoryFields.getAttach()).isNull();
        assertThat(factoryFields.getKeyboard()).isNull();
    }

    @Test
    @DisplayName("Tests method.command.CommandAnswerRequest serialization, factory method, and botToken masking")
    void testCommandAnswerRequestSerialization() {
        CommandAnswerFields fields = CommandAnswerFields.of("Here is the help text...");

        CommandAnswerRequest request = CommandAnswerRequest.builder()
                .botId(456L)
                .botToken("secret_bot_token")
                .commandId(42L)
                .messageId(789L)
                .dialogId("chat5")
                .fields(fields)
                .build();

        assertThat(request.toString()).doesNotContain("secret_bot_token");

        String json = BitrixJsonMapper.toJson(request);
        assertThat(json)
                .contains("\"botId\":456")
                .contains("\"botToken\":\"secret_bot_token\"")
                .contains("\"commandId\":42")
                .contains("\"messageId\":789")
                .contains("\"dialogId\":\"chat5\"")
                .contains("\"fields\":{")
                .contains("\"message\":\"Here is the help text...\"");

        CommandAnswerRequest deserialized = BitrixJsonMapper.fromJson(json, CommandAnswerRequest.class);
        assertThat(deserialized).isEqualTo(request);

        CommandAnswerRequest factoryRequest = CommandAnswerRequest.of(42L, 789L, "chat5", "Quick reply");
        assertThat(factoryRequest.getCommandId()).isEqualTo(42L);
        assertThat(factoryRequest.getMessageId()).isEqualTo(789L);
        assertThat(factoryRequest.getDialogId()).isEqualTo("chat5");
        assertThat(factoryRequest.getFields()).isEqualTo(CommandAnswerFields.of("Quick reply"));
        assertThat(factoryRequest.getBotId()).isNull();
        assertThat(factoryRequest.getBotToken()).isNull();
    }
}
