package io.github.bitrix24.bots.api.model.file;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Objects;

/**
 * Fields payload for {@code imbot.v2.File.upload}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileUploadFields {

    /**
     * File name with extension (e.g. {@code "report.pdf"}).
     */
    private String name;

    /**
     * Base64-encoded file content (up to 100 MB).
     */
    @ToString.Exclude
    private String content;

    /**
     * Optional message text sent together with the file.
     */
    private String message;

    public static FileUploadFields ofBase64(String name, String base64Content) {
        return FileUploadFields.builder()
                .name(name)
                .content(base64Content)
                .build();
    }

    public static FileUploadFields ofBase64(String name, String base64Content, String message) {
        return FileUploadFields.builder()
                .name(name)
                .content(base64Content)
                .message(message)
                .build();
    }

    public static FileUploadFields ofBytes(String name, byte[] fileBytes) {
        return ofBytes(name, fileBytes, null);
    }

    public static FileUploadFields ofBytes(String name, byte[] fileBytes, String message) {
        Objects.requireNonNull(fileBytes, "fileBytes cannot be null");
        String encoded = Base64.getEncoder().encodeToString(fileBytes);
        return ofBase64(name, encoded, message);
    }

    public static FileUploadFields ofPath(Path path) throws IOException {
        return ofPath(path, null);
    }

    public static FileUploadFields ofPath(Path path, String message) throws IOException {
        Objects.requireNonNull(path, "path cannot be null");
        byte[] bytes = Files.readAllBytes(path);
        String fileName = path.getFileName() != null ? path.getFileName().toString() : "file";
        return ofBytes(fileName, bytes, message);
    }
}
