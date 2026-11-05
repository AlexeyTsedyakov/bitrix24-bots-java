package io.github.bitrix24.bots.sdk.config;

import io.github.bitrix24.bots.api.model.event.EventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BotConfigTest {

    @Test
    @DisplayName("Default BotConfig has all event types allowed")
    void testDefaultAllowedEvents() {
        BotConfig config = BotConfig.builder().build();

        Set<EventType> allowed = config.getAllowedEvents();
        assertThat(allowed).contains(
                EventType.ONIMBOTV2MESSAGEADD,
                EventType.ONIMBOTV2COMMANDADD,
                EventType.ONIMBOTV2JOINCHAT,
                EventType.ONIMBOTV2DELETE,
                EventType.ONIMBOTV2MESSAGEUPDATE,
                EventType.ONIMBOTV2MESSAGEDELETE,
                EventType.ONIMBOTV2REACTIONCHANGE,
                EventType.ONIMBOTV2CONTEXTGET,
                EventType.UNKNOWN
        );
        assertThat(config.isAllowed(EventType.ONIMBOTV2MESSAGEADD)).isTrue();
        assertThat(config.isAllowed(null)).isFalse();
    }

    @Test
    @DisplayName("Custom allowed events correctly limits allowed types")
    void testCustomAllowedEvents() {
        BotConfig config = BotConfig.builder()
                .allowedEvents(EnumSet.of(EventType.ONIMBOTV2MESSAGEADD))
                .applicationToken("app_token_secret")
                .botToken("bot_token_secret")
                .botId(123L)
                .botCode("my_bot")
                .build();

        assertThat(config.isAllowed(EventType.ONIMBOTV2MESSAGEADD)).isTrue();
        assertThat(config.isAllowed(EventType.ONIMBOTV2COMMANDADD)).isFalse();
        assertThat(config.getApplicationToken()).isEqualTo("app_token_secret");
        assertThat(config.getBotToken()).isEqualTo("bot_token_secret");
        assertThat(config.getBotId()).isEqualTo(123L);
        assertThat(config.getBotCode()).isEqualTo("my_bot");
    }

    @Test
    @DisplayName("Empty or null allowedEvents safely defaults to all")
    void testEmptyAllowedEventsDefaultsToAll() {
        BotConfig config = BotConfig.builder().allowedEvents(null).build();
        assertThat(config.isAllowed(EventType.ONIMBOTV2MESSAGEADD)).isTrue();
        assertThat(config.getAllowedEvents()).contains(EventType.ONIMBOTV2MESSAGEADD);
    }
}
