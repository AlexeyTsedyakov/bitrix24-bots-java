package io.github.bitrix24.bots.api.model.method.file;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.bitrix24.bots.api.model.object.File;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Result payload returned by {@code imbot.v2.File.upload}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileUploadResult {

    private File file;
    private Long messageId;
    private Long chatId;
    private String dialogId;
}
