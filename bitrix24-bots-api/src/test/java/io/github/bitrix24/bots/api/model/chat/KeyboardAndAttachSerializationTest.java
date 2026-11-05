package io.github.bitrix24.bots.api.model.chat;

import io.github.bitrix24.bots.api.json.BitrixJsonMapper;
import io.github.bitrix24.bots.api.model.chat.attach.Attach;
import io.github.bitrix24.bots.api.model.chat.keyboard.Keyboard;
import io.github.bitrix24.bots.api.model.chat.keyboard.KeyboardButton;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeyboardAndAttachSerializationTest {

    @Test
    @DisplayName("Should serialize Keyboard and Buttons with uppercase property names")
    void testKeyboardSerialization() {
        Keyboard keyboard = Keyboard.of(
                KeyboardButton.link("Bitrix24 Docs", "https://apidocs.bitrix24.ru"),
                KeyboardButton.builder()
                        .text("Trigger Command")
                        .command("help")
                        .commandParams("full")
                        .bgColorToken("primary")
                        .build(),
                KeyboardButton.put("Put Text", "/help"),
                KeyboardButton.send("Send Text", "I need assistance")
        );

        String json = BitrixJsonMapper.toJson(keyboard);

        assertThat(json).contains("\"TEXT\":\"Bitrix24 Docs\"");
        assertThat(json).contains("\"LINK\":\"https://apidocs.bitrix24.ru\"");
        assertThat(json).contains("\"COMMAND\":\"help\"");
        assertThat(json).contains("\"COMMAND_PARAMS\":\"full\"");
        assertThat(json).contains("\"BG_COLOR_TOKEN\":\"primary\"");
        assertThat(json).contains("\"ACTION\":\"PUT\"");
        assertThat(json).contains("\"ACTION_VALUE\":\"/help\"");
        assertThat(json).contains("\"ACTION\":\"SEND\"");
    }

    @Test
    @DisplayName("Should serialize Attach card with uppercase properties and various blocks")
    void testAttachSerialization() {
        Attach attach = Attach.builder()
                .id("card1")
                .color("#29619b")
                .colorToken("primary")
                .build()
                .addMessage("Notice title")
                .addLink("Details", "https://example.com", "Description text", "https://example.com/preview.png")
                .addImage("Diagram", "https://example.com/img.png", null)
                .addDelimiter();

        String json = BitrixJsonMapper.toJson(attach);

        assertThat(json).contains("\"ID\":\"card1\"");
        assertThat(json).contains("\"COLOR\":\"#29619b\"");
        assertThat(json).contains("\"COLOR_TOKEN\":\"primary\"");
        assertThat(json).contains("\"BLOCKS\":[");
        assertThat(json).contains("\"MESSAGE\":\"Notice title\"");
        assertThat(json).contains("\"LINK\":{");
        assertThat(json).contains("\"NAME\":\"Details\"");
        assertThat(json).contains("\"LINK\":\"https://example.com\"");
        assertThat(json).contains("\"IMAGE\":{");
        assertThat(json).contains("\"NAME\":\"Diagram\"");
        assertThat(json).contains("\"DELIMITER\":{\"DELIMITER\":\"Y\"}");
    }
}
