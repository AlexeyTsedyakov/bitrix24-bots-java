package io.github.bitrix24.bots.api.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard error response structure returned by Bitrix24 REST API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BitrixErrorResponse {

    /**
     * Error code (e.g., BOT_NOT_FOUND, INVALID_CREDENTIALS, QUERY_LIMIT_EXCEEDED).
     */
    private String error;

    /**
     * Human-readable description of the error.
     */
    @JsonProperty("error_description")
    private String errorDescription;

    /**
     * Additional error details if provided by the endpoint.
     */
    @JsonProperty("error_information")
    private Object errorInformation;

    /**
     * Exception class or technical detail name if provided.
     */
    @JsonProperty("error_exception")
    private String errorException;

    /**
     * Argument name that caused the validation failure if applicable.
     */
    private String argument;
}
