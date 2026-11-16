package io.github.bitrix24.bots.api.model.object;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import tools.jackson.databind.JsonNode;

import java.time.Instant;

/**
 * Metadata of a file attached to a message or uploaded via {@code imbot.v2.File.upload}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class File {

    private Long id;
    private Long chatId;
    private Instant date;
    private String type;
    private String name;
    private String extension;
    private Long size;
    private Integer duration;
    private JsonNode image;
    private Long authorId;
    private String authorName;
    private Boolean isTranscribable;
    private Boolean isVideoNote;
    private Boolean isVoiceNote;
}
