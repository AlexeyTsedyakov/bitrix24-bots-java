package io.github.bitrix24.bots.api.model.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * User details provided in Bitrix24 bot events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo {

    private Long id;
    private Boolean active;
    private String name;

    @JsonAlias({"first_name", "firstName"})
    private String firstName;

    @JsonAlias({"last_name", "lastName"})
    private String lastName;

    @JsonAlias({"work_position", "workPosition"})
    private String workPosition;

    private String color;
    private String avatar;
    private String gender;
    private String birthday;
    private Boolean extranet;
    private Boolean bot;
    private Boolean connector;

    @JsonAlias({"external_auth_id", "externalAuthId"})
    private String externalAuthId;

    private String status;
    private Boolean idle;

    @JsonAlias({"last_activity_date", "lastActivityDate"})
    private String lastActivityDate;

    private Boolean absent;
    private List<Long> departments;
    private String type;

    @JsonAlias({"is_admin", "isAdmin", "admin"})
    private Boolean isAdmin;
}
