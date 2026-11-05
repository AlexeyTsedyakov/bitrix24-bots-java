package io.github.bitrix24.bots.api.model.chat.attach;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representation of a structured ATTACH card in Bitrix24 messages.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Attach {

    @JsonProperty("ID")
    private String id;

    @JsonProperty("COLOR")
    private String color;

    @JsonProperty("COLOR_TOKEN")
    private String colorToken;

    @JsonProperty("BLOCKS")
    @Builder.Default
    private List<Map<String, Object>> blocks = new ArrayList<>();

    public Attach addBlock(Map<String, Object> block) {
        if (blocks == null) {
            blocks = new ArrayList<>();
        }
        blocks.add(block);
        return this;
    }

    public Attach addMessage(String text) {
        Map<String, Object> block = new HashMap<>();
        block.put("MESSAGE", text);
        return addBlock(block);
    }

    public Attach addLink(String name, String link, String desc, String preview) {
        Map<String, Object> block = new HashMap<>();
        Map<String, Object> linkData = new HashMap<>();
        linkData.put("NAME", name);
        linkData.put("LINK", link);
        if (desc != null) {
            linkData.put("DESC", desc);
        }
        if (preview != null) {
            linkData.put("PREVIEW", preview);
        }
        block.put("LINK", linkData);
        return addBlock(block);
    }

    public Attach addImage(String name, String link, String preview) {
        Map<String, Object> block = new HashMap<>();
        Map<String, Object> imgData = new HashMap<>();
        imgData.put("NAME", name);
        imgData.put("LINK", link);
        if (preview != null) {
            imgData.put("PREVIEW", preview);
        }
        block.put("IMAGE", imgData);
        return addBlock(block);
    }

    public Attach addFile(String name, String link) {
        return addFile(name, link, null);
    }

    public Attach addFile(String name, String link, Long size) {
        Map<String, Object> block = new HashMap<>();
        Map<String, Object> fileData = new HashMap<>();
        fileData.put("NAME", name);
        fileData.put("LINK", link);
        if (size != null) {
            fileData.put("SIZE", size);
        }
        block.put("FILE", fileData);
        return addBlock(block);
    }

    public Attach addDelimiter() {
        Map<String, Object> block = new HashMap<>();
        Map<String, Object> delim = new HashMap<>();
        delim.put("DELIMITER", "Y");
        block.put("DELIMITER", delim);
        return addBlock(block);
    }

    public Attach addGrid(List<Map<String, Object>> gridItems) {
        Map<String, Object> block = new HashMap<>();
        block.put("GRID", gridItems);
        return addBlock(block);
    }

    public static Attach ofMessage(String message) {
        return Attach.builder().build().addMessage(message);
    }
}
