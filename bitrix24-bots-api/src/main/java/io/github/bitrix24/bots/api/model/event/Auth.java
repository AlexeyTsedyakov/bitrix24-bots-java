package io.github.bitrix24.bots.api.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class Auth {

    @ToString.Exclude
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private Long expiresIn;

    private String scope;
    private String domain;

    @JsonProperty("server_endpoint")
    private String serverEndpoint;

    private String status;

    @JsonProperty("client_endpoint")
    private String clientEndpoint;

    @JsonProperty("member_id")
    private String memberId;

    @ToString.Exclude
    @JsonProperty("refresh_token")
    private String refreshToken;

    @ToString.Exclude
    @JsonProperty("application_token")
    private String applicationToken;
}
