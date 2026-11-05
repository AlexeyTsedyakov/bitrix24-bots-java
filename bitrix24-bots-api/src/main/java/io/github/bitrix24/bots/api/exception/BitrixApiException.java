package io.github.bitrix24.bots.api.exception;

import io.github.bitrix24.bots.api.model.common.BitrixErrorResponse;
import lombok.Getter;

/**
 * Exception thrown when a Bitrix24 REST API call fails or returns an error.
 */
@Getter
public class BitrixApiException extends RuntimeException {

    private final String error;
    private final String errorDescription;
    private final int httpStatusCode;
    private final Object errorInformation;

    public BitrixApiException(String error, String errorDescription) {
        this(error, errorDescription, 0, null, null);
    }

    public BitrixApiException(String error, String errorDescription, int httpStatusCode) {
        this(error, errorDescription, httpStatusCode, null, null);
    }

    public BitrixApiException(String error, String errorDescription, int httpStatusCode, Throwable cause) {
        this(error, errorDescription, httpStatusCode, null, cause);
    }

    public BitrixApiException(String error, String errorDescription, int httpStatusCode, Object errorInformation, Throwable cause) {
        super(formatMessage(error, errorDescription, httpStatusCode), cause);
        this.error = error;
        this.errorDescription = errorDescription;
        this.httpStatusCode = httpStatusCode;
        this.errorInformation = errorInformation;
    }

    public BitrixApiException(BitrixErrorResponse errorResponse, int httpStatusCode) {
        this(errorResponse != null ? errorResponse.getError() : "UNKNOWN_ERROR",
             errorResponse != null ? errorResponse.getErrorDescription() : null,
             httpStatusCode,
             errorResponse != null ? errorResponse.getErrorInformation() : null,
             null);
    }

    public BitrixApiException(String message) {
        super(message);
        this.error = null;
        this.errorDescription = message;
        this.httpStatusCode = 0;
        this.errorInformation = null;
    }

    public BitrixApiException(String message, Throwable cause) {
        super(message, cause);
        this.error = null;
        this.errorDescription = message;
        this.httpStatusCode = 0;
        this.errorInformation = null;
    }

    private static String formatMessage(String error, String errorDescription, int httpStatusCode) {
        StringBuilder sb = new StringBuilder();
        if (httpStatusCode > 0) {
            sb.append("HTTP ").append(httpStatusCode).append(" - ");
        }
        if (error != null && !error.isBlank()) {
            sb.append("[").append(error).append("] ");
        }
        if (errorDescription != null && !errorDescription.isBlank()) {
            sb.append(errorDescription);
        } else if (error == null || error.isBlank()) {
            sb.append("Bitrix24 API request failed");
        }
        return sb.toString().trim();
    }
}
