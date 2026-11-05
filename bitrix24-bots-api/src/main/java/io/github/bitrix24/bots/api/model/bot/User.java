package io.github.bitrix24.bots.api.model.bot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * Representation of a Bitrix24 User associated with a Bot.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private Long id;
    private Boolean active;
    private String name;
    private String firstName;
    private String lastName;
    private String workPosition;
    private String color;
    private String avatar;
    private String gender;
    private String birthday;
    private Boolean extranet;
    private Boolean bot;
    private Boolean connector;
    private String externalAuthId;
    private String status;
    private Boolean idle;
    private String lastActivityDate;
    private Boolean absent;
    private List<Long> departments;
    private String type;
}
