package io.github.bitrix24.bots.api.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.bitrix24.bots.api.exception.BitrixApiException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic wrapper for all Bitrix24 REST API responses.
 *
 * @param <T> type of the payload contained in the {@code result} field
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BitrixResponse<T> {

    /**
     * Payload returned by the REST API method on success.
     */
    private T result;

    /**
     * Timing metadata of the request execution.
     */
    private BitrixTime time;

    /**
     * Total number of items (for list / paginated methods).
     */
    private Integer total;

    /**
     * Next offset for list pagination if there are more records.
     */
    private Integer next;

    /**
     * Error code if the call failed (e.g., BOT_NOT_FOUND, INVALID_CREDENTIALS).
     */
    private String error;

    /**
     * Error description explaining why the call failed.
     */
    @JsonProperty("error_description")
    private String errorDescription;

    /**
     * Optional additional error information.
     */
    @JsonProperty("error_information")
    private Object errorInformation;

    /**
     * Returns true if the response represents a successful operation.
     *
     * @return true if error is null or blank
     */
    public boolean isSuccess() {
        return error == null || error.isBlank();
    }

    /**
     * Returns true if the response represents an error.
     *
     * @return true if error code is present
     */
    public boolean isError() {
        return !isSuccess();
    }

    /**
     * Returns the payload if the request was successful, otherwise throws {@link BitrixApiException}.
     *
     * @return the result payload
     * @throws BitrixApiException if the response contains an error
     */
    public T getResultOrThrow() {
        if (isError()) {
            throw new BitrixApiException(error, errorDescription, 0, errorInformation, null);
        }
        return result;
    }

    /**
     * Creates a successful BitrixResponse with the given result payload.
     *
     * @param result payload
     * @param <T> payload type
     * @return successful response
     */
    public static <T> BitrixResponse<T> ok(T result) {
        return BitrixResponse.<T>builder()
                .result(result)
                .build();
    }

    /**
     * Creates an error BitrixResponse with the given error code and description.
     *
     * @param error error code
     * @param errorDescription error description
     * @param <T> payload type
     * @return error response
     */
    public static <T> BitrixResponse<T> error(String error, String errorDescription) {
        return BitrixResponse.<T>builder()
                .error(error)
                .errorDescription(errorDescription)
                .build();
    }
}
