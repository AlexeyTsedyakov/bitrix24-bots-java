package io.github.bitrix24.bots.api.model.method.file;

import io.github.bitrix24.bots.api.json.BitrixJsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FileModelTest {

    @Test
    @DisplayName("Tests method.file.FileUploadFields serialization, factory methods, and content masking")
    void testFileUploadFieldsSerialization(@TempDir Path tempDir) throws IOException {
        FileUploadFields fields = FileUploadFields.builder()
                .name("report.pdf")
                .content("SGVsbG8gV29ybGQh")
                .message("Here is the report")
                .build();

        assertThat(fields.toString()).doesNotContain("SGVsbG8gV29ybGQh");

        String json = BitrixJsonMapper.toJson(fields);
        assertThat(json)
                .contains("\"name\":\"report.pdf\"")
                .contains("\"content\":\"SGVsbG8gV29ybGQh\"")
                .contains("\"message\":\"Here is the report\"");

        FileUploadFields deserialized = BitrixJsonMapper.fromJson(json, FileUploadFields.class);
        assertThat(deserialized).isEqualTo(fields);

        FileUploadFields fromBase64 = FileUploadFields.ofBase64("note.txt", "SGVsbG8=");
        assertThat(fromBase64.getName()).isEqualTo("note.txt");
        assertThat(fromBase64.getContent()).isEqualTo("SGVsbG8=");
        assertThat(fromBase64.getMessage()).isNull();
        assertThat(BitrixJsonMapper.toJson(fromBase64)).doesNotContain("\"message\"");

        FileUploadFields fromBytes = FileUploadFields.ofBytes(
                "report.pdf",
                "Hello World!".getBytes(StandardCharsets.UTF_8),
                "Here is the report"
        );
        assertThat(fromBytes).isEqualTo(fields);

        Path tempFile = tempDir.resolve("report.pdf");
        Files.writeString(tempFile, "Hello World!", StandardCharsets.UTF_8);
        FileUploadFields fromPath = FileUploadFields.ofPath(tempFile, "Here is the report");
        assertThat(fromPath).isEqualTo(fields);
    }

    @Test
    @DisplayName("Tests method.file.FileUploadRequest serialization, factory methods, and botToken masking")
    void testFileUploadRequestSerialization(@TempDir Path tempDir) throws IOException {
        FileUploadFields fields = FileUploadFields.ofBase64("report.pdf", "SGVsbG8gV29ybGQh", "Here is the report");

        FileUploadRequest request = FileUploadRequest.builder()
                .botId(456L)
                .botToken("secret_bot_token")
                .dialogId("chat5")
                .fields(fields)
                .build();

        assertThat(request.toString()).doesNotContain("secret_bot_token");

        String json = BitrixJsonMapper.toJson(request);
        assertThat(json)
                .contains("\"botId\":456")
                .contains("\"botToken\":\"secret_bot_token\"")
                .contains("\"dialogId\":\"chat5\"")
                .contains("\"fields\":{")
                .contains("\"name\":\"report.pdf\"")
                .contains("\"content\":\"SGVsbG8gV29ybGQh\"")
                .contains("\"message\":\"Here is the report\"");

        FileUploadRequest deserialized = BitrixJsonMapper.fromJson(json, FileUploadRequest.class);
        assertThat(deserialized).isEqualTo(request);

        FileUploadRequest expectedFactory = FileUploadRequest.of("chat5", fields);
        assertThat(expectedFactory.getDialogId()).isEqualTo("chat5");
        assertThat(expectedFactory.getFields()).isEqualTo(fields);
        assertThat(expectedFactory.getBotId()).isNull();
        assertThat(expectedFactory.getBotToken()).isNull();

        assertThat(FileUploadRequest.ofBase64("chat5", "report.pdf", "SGVsbG8gV29ybGQh", "Here is the report"))
                .isEqualTo(expectedFactory);
        assertThat(FileUploadRequest.ofBytes("chat5", "report.pdf", "Hello World!".getBytes(StandardCharsets.UTF_8), "Here is the report"))
                .isEqualTo(expectedFactory);

        Path tempFile = tempDir.resolve("report.pdf");
        Files.writeString(tempFile, "Hello World!", StandardCharsets.UTF_8);
        assertThat(FileUploadRequest.ofPath("chat5", tempFile, "Here is the report"))
                .isEqualTo(expectedFactory);
    }

    @Test
    @DisplayName("Tests method.file.FileUploadResult deserialization against imbot.v2.File.upload example")
    void testFileUploadResultDeserialization() {
        String json = """
                {
                    "file": {
                        "id": 138,
                        "chatId": 5,
                        "name": "report.pdf",
                        "extension": "pdf",
                        "size": 35341
                    },
                    "messageId": 123,
                    "chatId": 5,
                    "dialogId": "chat5"
                }
                """;

        FileUploadResult result = BitrixJsonMapper.fromJson(json, FileUploadResult.class);
        assertThat(result).isNotNull();
        assertThat(result.getFile()).isNotNull();
        assertThat(result.getFile().getId()).isEqualTo(138L);
        assertThat(result.getFile().getChatId()).isEqualTo(5L);
        assertThat(result.getFile().getName()).isEqualTo("report.pdf");
        assertThat(result.getFile().getExtension()).isEqualTo("pdf");
        assertThat(result.getFile().getSize()).isEqualTo(35341L);
        assertThat(result.getMessageId()).isEqualTo(123L);
        assertThat(result.getChatId()).isEqualTo(5L);
        assertThat(result.getDialogId()).isEqualTo("chat5");
    }

    @Test
    @DisplayName("Tests method.file.FileDownloadRequest serialization, factory method, and botToken masking")
    void testFileDownloadRequestSerialization() {
        FileDownloadRequest request = FileDownloadRequest.builder()
                .botId(456L)
                .botToken("secret_bot_token")
                .fileId(138L)
                .build();

        assertThat(request.toString()).doesNotContain("secret_bot_token");

        String json = BitrixJsonMapper.toJson(request);
        assertThat(json)
                .contains("\"botId\":456")
                .contains("\"botToken\":\"secret_bot_token\"")
                .contains("\"fileId\":138");

        FileDownloadRequest deserialized = BitrixJsonMapper.fromJson(json, FileDownloadRequest.class);
        assertThat(deserialized).isEqualTo(request);

        FileDownloadRequest factoryRequest = FileDownloadRequest.of(138L);
        assertThat(factoryRequest.getFileId()).isEqualTo(138L);
        assertThat(factoryRequest.getBotId()).isNull();
        assertThat(factoryRequest.getBotToken()).isNull();
    }

    @Test
    @DisplayName("Tests method.file.FileDownloadResult deserialization against imbot.v2.File.download example")
    void testFileDownloadResultDeserialization() {
        String json = """
                {
                    "downloadUrl": "https://portal.bitrix24.ru/rest/download.json?token=imbot%7CaW..."
                }
                """;

        FileDownloadResult result = BitrixJsonMapper.fromJson(json, FileDownloadResult.class);
        assertThat(result).isNotNull();
        assertThat(result.getDownloadUrl())
                .isEqualTo("https://portal.bitrix24.ru/rest/download.json?token=imbot%7CaW...");
    }
}
