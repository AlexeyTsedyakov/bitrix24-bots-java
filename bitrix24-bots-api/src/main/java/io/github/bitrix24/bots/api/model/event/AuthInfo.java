package io.github.bitrix24.bots.api.model.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * Authentication information supplied with Bitrix24 bot event notifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthInfo {

    private String domain;

    @ToString.Exclude
    @JsonAlias({"application_token", "applicationToken"})
    private String applicationToken;

    @ToString.Exclude
    @JsonAlias({"access_token", "accessToken"})
    private String accessToken;

    private String expires;

    @JsonAlias({"expires_in", "expiresIn"})
    private Long expiresIn;

    private String scope;

    @JsonAlias({"server_endpoint", "serverEndpoint"})
    private String serverEndpoint;

    private String status;

    @JsonAlias({"client_endpoint", "clientEndpoint"})
    private String clientEndpoint;

    @JsonAlias({"member_id", "memberId"})
    private String memberId;

    @JsonAlias({"user_id", "userId"})
    private String userId;

    @ToString.Exclude
    @JsonAlias({"refresh_token", "refreshToken"})
    private String refreshToken;
}
