package io.github.bitrix24.bots.api.model.file;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Request payload for {@code imbot.v2.File.upload}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileUploadRequest {

    private Long botId;
    @ToString.Exclude
    private String botToken;
    private String dialogId;
    private FileUploadFields fields;

    public static FileUploadRequest of(String dialogId, FileUploadFields fields) {
        return FileUploadRequest.builder()
                .dialogId(dialogId)
                .fields(fields)
                .build();
    }

    public static FileUploadRequest ofBase64(String dialogId, String name, String base64Content, String message) {
        return of(dialogId, FileUploadFields.ofBase64(name, base64Content, message));
    }

    public static FileUploadRequest ofBytes(String dialogId, String name, byte[] fileBytes, String message) {
        return of(dialogId, FileUploadFields.ofBytes(name, fileBytes, message));
    }

    public static FileUploadRequest ofPath(String dialogId, Path path, String message) throws IOException {
        return of(dialogId, FileUploadFields.ofPath(path, message));
    }
}
