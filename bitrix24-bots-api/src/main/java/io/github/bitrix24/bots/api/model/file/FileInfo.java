package io.github.bitrix24.bots.api.model.file;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Metadata of a file uploaded to a Bitrix24 chat via {@code imbot.v2.File.upload}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileInfo {

    private Long id;
    private Long chatId;
    private String name;
    private String extension;
    private Long size;
}
