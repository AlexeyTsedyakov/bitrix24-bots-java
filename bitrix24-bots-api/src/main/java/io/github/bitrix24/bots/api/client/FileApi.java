package io.github.bitrix24.bots.api.client;

import io.github.bitrix24.bots.api.model.common.BitrixResponse;
import io.github.bitrix24.bots.api.model.file.*;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Client interface for {@code imbot.v2.File.*} methods.
 */
public interface FileApi {

    /**
     * Uploads a file to Bitrix24 Disk and sends it to the specified chat dialog via {@code imbot.v2.File.upload}.
     * Note: {@code imbot.v2.File.upload} cannot be executed inside a {@code batch} request.
     *
     * @param request file upload request
     * @return response containing uploaded file metadata and created message ID
     */
    BitrixResponse<FileUploadResult> upload(FileUploadRequest request);

    default BitrixResponse<FileUploadResult> upload(String dialogId, FileUploadFields fields) {
        return upload(FileUploadRequest.of(dialogId, fields));
    }

    default BitrixResponse<FileUploadResult> uploadBase64(String dialogId, String name, String base64Content) {
        return upload(FileUploadRequest.ofBase64(dialogId, name, base64Content, null));
    }

    default BitrixResponse<FileUploadResult> uploadBase64(String dialogId, String name, String base64Content, String message) {
        return upload(FileUploadRequest.ofBase64(dialogId, name, base64Content, message));
    }

    default BitrixResponse<FileUploadResult> uploadBytes(String dialogId, String name, byte[] fileBytes) {
        return upload(FileUploadRequest.ofBytes(dialogId, name, fileBytes, null));
    }

    default BitrixResponse<FileUploadResult> uploadBytes(String dialogId, String name, byte[] fileBytes, String message) {
        return upload(FileUploadRequest.ofBytes(dialogId, name, fileBytes, message));
    }

    default BitrixResponse<FileUploadResult> uploadPath(String dialogId, Path path) throws IOException {
        return upload(FileUploadRequest.ofPath(dialogId, path, null));
    }

    default BitrixResponse<FileUploadResult> uploadPath(String dialogId, Path path, String message) throws IOException {
        return upload(FileUploadRequest.ofPath(dialogId, path, message));
    }

    /**
     * Retrieves a one-time authorized download link for a chat file via {@code imbot.v2.File.download}.
     *
     * @param request file download request
     * @return response containing {@code downloadUrl}
     */
    BitrixResponse<FileDownloadResult> download(FileDownloadRequest request);

    default BitrixResponse<FileDownloadResult> download(Long fileId) {
        return download(FileDownloadRequest.of(fileId));
    }
}
