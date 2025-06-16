package pl.niebieskieaparaty.imageuploader.upload.core;

import java.time.LocalDateTime;

public record UploadedData(
        String fileName,
        String eventId,
        String username,
        String originalFileObjectKey,
        String originalFilePresignedUrl,
        String compressedFileObjectKey,
        String compressedFilePresignedUrl,
        LocalDateTime presignDateTime
) {
    public static UploadedData createNew(
            final String fileName,
            final String eventId,
            final String username,
            final String originalFileObjectKey,
            final String originalFilePresignedUrl,
            final String compressedFileObjectKey,
            final String compressedFilePresignedUrl
    ) {
        return new UploadedData(
                fileName,
                eventId,
                username,
                originalFileObjectKey,
                originalFilePresignedUrl,
                compressedFileObjectKey,
                compressedFilePresignedUrl,
                LocalDateTime.now()
        );
    }
}
