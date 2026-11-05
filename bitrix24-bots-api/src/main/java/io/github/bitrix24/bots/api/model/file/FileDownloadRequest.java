package io.github.bitrix24.bots.api.model.file;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * Request payload for {@code imbot.v2.File.download}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileDownloadRequest {

    private Long botId;
    @ToString.Exclude
    private String botToken;
    private Long fileId;

    public static FileDownloadRequest of(Long fileId) {
        return FileDownloadRequest.builder()
                .fileId(fileId)
                .build();
    }
}
