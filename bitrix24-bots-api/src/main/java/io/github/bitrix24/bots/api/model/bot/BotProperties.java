package io.github.bitrix24.bots.api.model.bot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Profile properties of a Bitrix24 Bot (name, avatar, position, etc.).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class BotProperties {

    private String name;
    private String firstName;
    private String lastName;
    private String workPosition;
    private String color;
    private String avatar;
    private String gender;
    private String birthday;
}
