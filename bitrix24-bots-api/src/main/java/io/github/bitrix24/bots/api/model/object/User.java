package io.github.bitrix24.bots.api.model.object;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;

/**
 * User details provided in Bitrix24 bot events and {@code imbot.v2} / {@code im.v2} responses.
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
    private Instant lastActivityDate;
    private Instant mobileLastDate;
    private Instant desktopLastDate;
    private Boolean absent;
    private List<Long> departments;
    private JsonNode phones;
    private String type;
    private Boolean internalAccount;
    private Boolean intranetUser;
    private String website;
    private String email;
}
